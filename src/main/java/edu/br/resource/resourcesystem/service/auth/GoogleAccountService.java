package edu.br.resource.resourcesystem.service.auth;

import edu.br.resource.resourcesystem.model.entity.User;
import edu.br.resource.resourcesystem.model.enums.AccountRole;
import edu.br.resource.resourcesystem.model.enums.AccountStatus;
import edu.br.resource.resourcesystem.repository.InstitutionRepository;
import edu.br.resource.resourcesystem.repository.UserRepository;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GoogleAccountService {
    private final UserRepository users;
    private final InstitutionRepository institutions;
    private final AccountEmailRegistry emails;

    @Transactional
    public User resolve(OidcUser identity) {
        String subject = identity.getSubject();
        String email = identity.getEmail();
        if (!Boolean.TRUE.equals(identity.getEmailVerified())
                || subject == null
                || subject.isBlank()
                || subject.length() > 255
                || email == null
                || email.isBlank()
                || email.length() > 320) {
            throw rejected();
        }
        email = email.trim().toLowerCase(Locale.ROOT);
        emails.lock(email);

        var linked = users.findByOauthProviderAndOauthSubject("google", subject);
        var sameEmail = users.findByEmailIgnoreCase(email);
        if (institutions.findByEmailIgnoreCase(email).isPresent()
                || sameEmail.isPresent()
                        && (linked.isEmpty()
                                || !sameEmail.get().getId().equals(linked.get().getId()))) {
            throw rejected();
        }
        if (linked.isPresent()) {
            var account = linked.get();
            if (account.getRole() != AccountRole.DONOR
                    || account.getStatus() != AccountStatus.ACTIVE) {
                throw rejected();
            }

            if (institutions.findByEmailIgnoreCase(account.getEmail()).isPresent())
                throw rejected();
            return account;
        }
        String name = identity.getFullName();
        if (name == null || name.isBlank()) name = email;
        return users.saveAndFlush(
                User.builder()
                        .email(email)
                        .fullName(name.substring(0, Math.min(name.length(), 200)))
                        .role(AccountRole.DONOR)
                        .status(AccountStatus.ACTIVE)
                        .oauthProvider("google")
                        .oauthSubject(subject)
                        .build());
    }

    private OAuth2AuthenticationException rejected() {
        return new OAuth2AuthenticationException(
                new OAuth2Error("account_unavailable"),
                "Não foi possível acessar esta conta com Google.");
    }
}
