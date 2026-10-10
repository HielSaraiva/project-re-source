package edu.br.resource.resourcesystem.service.auth;

import edu.br.resource.resourcesystem.config.PasswordRecoveryProperties;
import edu.br.resource.resourcesystem.dto.request.PasswordResetRequest;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class PasswordRecoveryService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final JdbcTemplate db;
    private final PasswordEncoder passwords;
    private final PasswordRecoveryMailService mail;
    private final PasswordRecoveryProperties settings;
    private final SessionRegistry sessions;

    private record Account(int id, boolean institution, String email, String name, String password) {
        String table() { return institution ? "institutions" : "users"; }
        String tokenColumn() { return institution ? "institution_id" : "user_id"; }
    }

    @Transactional
    public void request(String email, String clientAddress) {
        if (!settings.enabled() || !allowRequest(clientAddress)) return;
        var accounts = db.query("""
                SELECT id, false AS institution, email, full_name AS name, password_hash FROM users WHERE lower(email) = lower(?)
                UNION ALL
                SELECT id, true AS institution, email, legal_name AS name, password_hash FROM institutions WHERE lower(email) = lower(?)
                """, (rs, row) -> new Account(rs.getInt("id"), rs.getBoolean("institution"), rs.getString("email"),
                rs.getString("name"), rs.getString("password_hash")), email, email);
        // Never create a local password for a Google-only account or choose an ambiguous identity.
        if (accounts.size() != 1 || accounts.getFirst().password() == null) return;
        Account account = accounts.getFirst();
        db.queryForObject("SELECT id FROM " + account.table() + " WHERE id = ? FOR UPDATE", Integer.class, account.id());
        Long recent = db.queryForObject("SELECT count(*) FROM password_reset_tokens WHERE " + account.tokenColumn()
                + " = ? AND created_at > now() - interval '1 minute'", Long.class, account.id());
        if (recent != null && recent > 0) return;
        byte[] bytes = new byte[32]; RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant now = Instant.now();
        db.update("INSERT INTO password_reset_tokens(token_hash, " + account.tokenColumn()
                + ", created_at, expires_at) VALUES (?, ?, ?, ?)", digest(token), account.id(), Timestamp.from(now),
                Timestamp.from(now.plus(settings.tokenTtl())));
        // A delivery failure rolls back issuance. Existing links remain usable until a successful reset.
        mail.send(account.email(), account.name(), token);
    }

    private boolean allowRequest(String address) {
        return db.update("""
                INSERT INTO password_reset_rate_limits(client_hash, window_started_at, requests) VALUES (?, now(), 1)
                ON CONFLICT (client_hash) DO UPDATE SET
                  window_started_at = CASE WHEN password_reset_rate_limits.window_started_at <= now() - interval '15 minutes'
                    THEN now() ELSE password_reset_rate_limits.window_started_at END,
                  requests = CASE WHEN password_reset_rate_limits.window_started_at <= now() - interval '15 minutes'
                    THEN 1 ELSE password_reset_rate_limits.requests + 1 END
                WHERE password_reset_rate_limits.window_started_at <= now() - interval '15 minutes'
                  OR password_reset_rate_limits.requests < 5
                """, digest(address)) == 1;
    }

    @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 3600000, initialDelay = 3600000)
    @Transactional
    public void cleanExpired() {
        db.update("DELETE FROM password_reset_tokens WHERE expires_at < now() - interval '1 day'");
        db.update("DELETE FROM password_reset_rate_limits WHERE window_started_at < now() - interval '1 day'");
    }

    @Transactional(readOnly = true)
    public boolean validToken(String token) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) return false;
        return Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM password_reset_tokens WHERE token_hash = ?"
                + " AND consumed_at IS NULL AND expires_at > now())", Boolean.class, digest(token)));
    }

    @Transactional
    public Optional<String> reset(@Valid PasswordResetRequest request) {
        String hash = digest(request.getToken());
        List<Account> accounts = db.query("""
                SELECT u.id, false AS institution, u.email, u.full_name AS name, u.password_hash
                  FROM password_reset_tokens t JOIN users u ON t.user_id = u.id WHERE t.token_hash = ?
                UNION ALL
                SELECT i.id, true AS institution, i.email, i.legal_name AS name, i.password_hash
                  FROM password_reset_tokens t JOIN institutions i ON t.institution_id = i.id WHERE t.token_hash = ?
                """, (rs, row) -> new Account(rs.getInt("id"), rs.getBoolean("institution"), rs.getString("email"),
                rs.getString("name"), rs.getString("password_hash")), hash, hash);
        if (accounts.size() != 1 || accounts.getFirst().password() == null) return Optional.empty();
        Account account = accounts.getFirst();
        // Serialize all resets for this account before locking/consuming its tokens.
        db.queryForObject("SELECT id FROM " + account.table() + " WHERE id = ? FOR UPDATE", Integer.class, account.id());
        int consumed = db.update("UPDATE password_reset_tokens SET consumed_at = now() WHERE token_hash = ?"
                + " AND consumed_at IS NULL AND expires_at > now()", hash);
        if (consumed != 1) return Optional.empty();
        db.update("UPDATE " + account.table() + " SET password_hash = ?, updated_at = now() WHERE id = ?",
                passwords.encode(request.getPassword()), account.id());
        db.update("UPDATE password_reset_tokens SET consumed_at = now() WHERE " + account.tokenColumn()
                + " = ? AND consumed_at IS NULL", account.id());
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { expireSessions(account.email()); }
        });
        return Optional.of(account.institution() ? "ong" : "donor");
    }

    private void expireSessions(String email) {
        for (Object principal : sessions.getAllPrincipals()) {
            String name = principal instanceof UserDetails user ? user.getUsername()
                    : principal instanceof AuthenticatedPrincipal user ? user.getName() : null;
            if (name != null && name.equalsIgnoreCase(email)) {
                sessions.getAllSessions(principal, false).forEach(session -> session.expireNow());
            }
        }
    }

    private static String digest(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException exception) { throw new IllegalStateException(exception); }
    }
}
