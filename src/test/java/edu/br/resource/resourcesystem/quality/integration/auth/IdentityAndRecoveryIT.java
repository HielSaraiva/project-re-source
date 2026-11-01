package edu.br.resource.resourcesystem.quality.integration.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.br.resource.resourcesystem.config.PasswordRecoveryProperties;
import edu.br.resource.resourcesystem.dto.request.*;
import edu.br.resource.resourcesystem.quality.support.QualityIntegrationSupport;
import edu.br.resource.resourcesystem.service.auth.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

@DisplayName("RN-016 a RN-019 — cadastro transacional e recuperação sem alterar perfil")
class IdentityAndRecoveryIT extends QualityIntegrationSupport {
    @Autowired DonorRegistrationService donors;
    @Autowired InstitutionRegistrationService institutions;
    @Autowired AccountEmailRegistry emails;
    @Autowired InstitutionDocumentStorage storage;
    @Autowired PasswordRecoveryService recovery;
    @Autowired PasswordRecoveryProperties settings;
    @Autowired SessionRegistry sessions;

    DonorRegistrationRequest request(String email) {
        var f = new DonorRegistrationRequest();
        f.setFullName(" Pessoa fictícia ");
        f.setEmail(email);
        f.setPassword("Quality12!");
        f.setPasswordConfirmation("Quality12!");
        return f;
    }

    InstitutionRegistrationRequest organization(String email) {
        var f = new InstitutionRegistrationRequest();
        f.setEmail(email);
        f.setPassword("Quality12!");
        f.setPasswordConfirmation("Quality12!");
        f.setCnpj("12ABC34501DE35");
        f.setRepresentativeCpf("12345678909");
        f.setRepresentativeFullName("Representante fictício");
        return f;
    }

    InstitutionDocumentStorage.Upload upload() {
        return storage.validate(
                new MockMultipartFile(
                        "identity",
                        "test.pdf",
                        "application/pdf",
                        "%PDF-1.7\nFake only\n%%EOF".getBytes(StandardCharsets.US_ASCII)),
                "identityDocument");
    }

    String token() {
        return "T".repeat(43);
    }

    String hash(String token) throws Exception {
        return HexFormat.of()
                .formatHex(
                        MessageDigest.getInstance("SHA-256")
                                .digest(token.getBytes(StandardCharsets.UTF_8)));
    }

    void pendingToken(int account, boolean institution, String token, Instant expires)
            throws Exception {
        db.update(
                "insert into password_reset_tokens(token_hash,"
                        + (institution ? "institution_id" : "user_id")
                        + ",created_at,expires_at) values(?,?,?,?)",
                hash(token),
                account,
                java.sql.Timestamp.from(expires.minusSeconds(1800)),
                java.sql.Timestamp.from(expires));
    }

    PasswordResetRequest reset(String token) {
        var r = new PasswordResetRequest();
        r.setToken(token);
        r.setPassword("Changed12!");
        r.setPasswordConfirmation("Changed12!");
        return r;
    }

    @Test
    void register_WithValidDonor_NormalizesAndHashesWithoutLoggingIn() {
        donors.register(request(" USER@EXAMPLE.TEST "));
        assertThat(db.queryForObject("select email from users", String.class))
                .isEqualTo("user@example.test");
        assertThat(
                        encoder.matches(
                                "Quality12!",
                                db.queryForObject("select password_hash from users", String.class)))
                .isTrue();
        assertThat(db.queryForObject("select role::text from users", String.class))
                .isEqualTo("donor");
        assertThat(db.queryForObject("select status::text from users", String.class))
                .isEqualTo("active");
        assertThat(count("transactional_mail_outbox")).isEqualTo(1);
    }

    @Test
    void register_WithDuplicateAcrossProfiles_RejectsWithoutPartialAccount() {
        institution("reserved");
        assertThatThrownBy(() -> donors.register(request("RESERVED@example.test")))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
        assertThat(count("users")).isZero();
    }

    @Test
    void register_WithConcurrentSameEmail_CreatesExactlyOne() throws Exception {
        var gate = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            Callable<Boolean> call =
                    () -> {
                        gate.await();
                        try {
                            donors.register(request("same@example.test"));
                            return true;
                        } catch (EmailAlreadyRegisteredException expected) {
                            return false;
                        }
                    };
            var a = pool.submit(call);
            var b = pool.submit(call);
            gate.countDown();
            assertThat(List.of(a.get(), b.get())).containsExactlyInAnyOrder(true, false);
        }
        assertThat(count("users")).isEqualTo(1);
        assertThat(count("transactional_mail_outbox")).isEqualTo(1);
    }

    @Test
    void register_WithValidInstitution_CreatesPendingAccountAndTwoPrivateDocuments()
            throws Exception {
        var request = organization("pending@example.test");
        var file = upload();
        institutions.register(
                request,
                new CnpjLookupService.Company(request.getCnpj(), "Organização fictícia"),
                file,
                file);
        assertThat(count("institutions")).isEqualTo(1);
        assertThat(count("institution_documents")).isEqualTo(2);
        assertThat(db.queryForObject("select status::text from institutions", String.class))
                .isEqualTo("pending_approval");
        for (String key :
                db.queryForList("select storage_key from institution_documents", String.class))
            assertThat(Files.exists(DOCUMENTS.resolve(key))).isTrue();
        assertThat(
                        db.queryForObject(
                                "select count(*) from institution_documents where status='pending'",
                                Long.class))
                .isEqualTo(2);
    }

    @Test
    void register_WithDatabaseRollback_RemovesPrivateFilesAndAccount() throws Exception {
        long before;
        try (var stream = Files.list(DOCUMENTS)) {
            before = stream.count();
        }
        transaction.executeWithoutResult(
                tx -> {
                    var request = organization("rollback@example.test");
                    var file = upload();
                    institutions.register(
                            request,
                            new CnpjLookupService.Company(
                                    request.getCnpj(), "Organização fictícia"),
                            file,
                            file);
                    tx.setRollbackOnly();
                });
        assertThat(count("institutions")).isZero();
        assertThat(count("institution_documents")).isZero();
        assertThat(count("transactional_mail_outbox")).isZero();
        try (var stream = Files.list(DOCUMENTS)) {
            assertThat(stream.count()).isEqualTo(before);
        }
    }

    @Test
    void register_WithRepeatedCnpjOrCpf_RejectsBeforeStoringDocuments() {
        var r = organization("first@example.test");
        var f = upload();
        institutions.register(r, new CnpjLookupService.Company(r.getCnpj(), "Org"), f, f);
        var duplicate = organization("other@example.test");
        assertThatThrownBy(
                        () ->
                                institutions.register(
                                        duplicate,
                                        new CnpjLookupService.Company(r.getCnpj(), "Org"),
                                        f,
                                        f))
                .isInstanceOf(RegistrationFieldException.class);
        duplicate.setCnpj("12ABC34501DE35");
        assertThatThrownBy(
                        () ->
                                institutions.register(
                                        duplicate,
                                        new CnpjLookupService.Company("OTHER000000000", "Org"),
                                        f,
                                        f))
                .isInstanceOf(RegistrationFieldException.class);
        assertThat(count("institution_documents")).isEqualTo(2);
    }

    @Test
    void request_WithEligibleAccount_CreatesOnlyHashedTokenAfterRateChecks() {
        int id = createUser("eligible");
        recovery.request("ELIGIBLE@example.test", "198.51.100.20");
        assertThat(count("password_reset_tokens")).isEqualTo(1);
        String digest =
                db.queryForObject("select token_hash from password_reset_tokens", String.class);
        assertThat(digest).hasSize(64).matches("[a-f0-9]+");
        assertThat(outgoing).hasSize(1);
        recovery.request("eligible@example.test", "198.51.100.20");
        assertThat(count("password_reset_tokens")).isEqualTo(1);
    }

    @Test
    void request_WithUnknownOrGoogleOnlyAccount_DoesNotIssueToken() {
        recovery.request("unknown@example.test", "198.51.100.1");
        db.update(
                "insert into users(full_name,email,oauth_provider,oauth_subject) values('Google"
                        + " fictício','google@example.test','google','subject-fake')");
        recovery.request("google@example.test", "198.51.100.2");
        assertThat(count("password_reset_tokens")).isZero();
        assertThat(outgoing).isEmpty();
    }

    @Test
    void request_WithMoreThanFiveAttemptsPerAddress_LimitsIssuance() {
        for (int i = 0; i < 6; i++) {
            int id = createUser("rate" + i);
            recovery.request("rate" + i + "@example.test", "198.51.100.99");
        }
        assertThat(count("password_reset_tokens")).isEqualTo(5);
    }

    @Test
    void request_WithExpiredRateWindow_AllowsAgain() {
        recovery.request("unknown@example.test", "198.51.100.99");
        db.update(
                "update password_reset_rate_limits set requests=5,window_started_at=now()-interval"
                        + " '16 minutes'");
        createUser("afterwindow");
        recovery.request("afterwindow@example.test", "198.51.100.99");
        assertThat(count("password_reset_tokens")).isEqualTo(1);
    }

    @Test
    void request_WithDisabledMail_DoesNotRevealOrIssueToken() {
        createUser("disabledmail");
        var disabled =
                new PasswordRecoveryService(
                        db,
                        encoder,
                        mock(PasswordRecoveryMailService.class),
                        mock(
                                edu.br.resource.resourcesystem.service.mail.TransactionalMailService
                                        .class),
                        new PasswordRecoveryProperties(
                                false,
                                "fake@example.test",
                                "http://127.0.0.1",
                                Duration.ofMinutes(30)),
                        sessions);
        disabled.request("disabledmail@example.test", "198.51.100.1");
        assertThat(count("password_reset_tokens")).isZero();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"short", "!"})
    void validToken_WithMalformedInput_ReturnsFalse(String value) {
        assertThat(recovery.validToken(value)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void reset_WithEligibleAccount_ConsumesAllTokensAndExpiresSessions(boolean organization)
            throws Exception {
        int id = organization ? institution("reset-org") : createUser("reset-user");
        String email = organization ? "reset-org@example.test" : "reset-user@example.test";
        pendingToken(id, organization, token(), Instant.now().plusSeconds(600));
        pendingToken(id, organization, "U".repeat(43), Instant.now().plusSeconds(600));
        var principal =
                User.withUsername(email)
                        .password("fake")
                        .roles(organization ? "ONG" : "DONOR")
                        .build();
        sessions.registerNewSession("quality-session", principal);
        assertThat(recovery.validToken(token())).isTrue();
        assertThat(recovery.reset(reset(token()))).contains(organization ? "institution" : "donor");
        assertThat(recovery.validToken(token())).isFalse();
        assertThat(recovery.validToken("U".repeat(43))).isFalse();
        assertThat(recovery.reset(reset(token()))).isEmpty();
        assertThat(sessions.getSessionInformation("quality-session").isExpired()).isTrue();
        String table = organization ? "institutions" : "users";
        assertThat(
                        encoder.matches(
                                "Changed12!",
                                db.queryForObject(
                                        "select password_hash from " + table + " where id=?",
                                        String.class,
                                        id)))
                .isTrue();
        assertThat(
                        db.queryForObject(
                                "select count(*) from transactional_mail_outbox where"
                                        + " security_email",
                                Long.class))
                .isEqualTo(1);
    }

    @Test
    void reset_WithExpiredOrUnknownToken_PreservesPassword() throws Exception {
        int id = createUser("expired");
        pendingToken(id, false, token(), Instant.now().minusSeconds(1));
        assertThat(recovery.reset(reset(token()))).isEmpty();
        assertThat(recovery.reset(reset("X".repeat(43)))).isEmpty();
        assertThat(
                        encoder.matches(
                                "Quality12!",
                                db.queryForObject(
                                        "select password_hash from users where id=?",
                                        String.class,
                                        id)))
                .isTrue();
    }

    @Test
    void reset_WithBlockedAccount_DoesNotReactivate() throws Exception {
        int id = createUser("blocked");
        db.update("update users set status='blocked' where id=?", id);
        pendingToken(id, false, token(), Instant.now().plusSeconds(600));
        assertThat(recovery.reset(reset(token()))).contains("donor");
        assertThat(db.queryForObject("select status::text from users where id=?", String.class, id))
                .isEqualTo("blocked");
    }

    @Test
    void cleanExpired_WithOldTokensAndRateLimits_RemovesOnlyObsolete() throws Exception {
        int id = createUser("cleanup");
        pendingToken(id, false, token(), Instant.now().minus(Duration.ofDays(2)));
        pendingToken(id, false, "U".repeat(43), Instant.now().plusSeconds(600));
        recovery.cleanExpired();
        assertThat(count("password_reset_tokens")).isEqualTo(1);
    }

    @Test
    void registrationHttp_WithForgedRole_IgnoresMassAssignment() throws Exception {
        mvc.perform(
                        post("/register/donor")
                                .with(csrf())
                                .param("fullName", "Pessoa fictícia")
                                .param("email", "http@example.test")
                                .param("password", "Quality12!")
                                .param("passwordConfirmation", "Quality12!")
                                .param("role", "administrator")
                                .param("status", "blocked"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));
        assertThat(db.queryForObject("select role::text from users", String.class))
                .isEqualTo("donor");
        assertThat(db.queryForObject("select status::text from users", String.class))
                .isEqualTo("active");
    }
}
