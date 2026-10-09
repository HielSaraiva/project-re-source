package edu.br.resource.resourcesystem.security;

import edu.br.resource.resourcesystem.service.auth.GoogleAccountService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoogleOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {
    private final GoogleAccountService accounts;
    private final OidcUserService delegate = new OidcUserService();

    @Override
    public OidcUser loadUser(OidcUserRequest request) throws OAuth2AuthenticationException {
        if (!"google".equals(request.getClientRegistration().getRegistrationId())) {
            throw new OAuth2AuthenticationException(new OAuth2Error("unsupported_provider"));
        }
        var identity = delegate.loadUser(request);
        try {
            var account = accounts.resolve(identity);
            // CurrentActor and the existing services look up the principal by local email.
            return new LocalGoogleUser(identity, account.getEmail());
        } catch (DataIntegrityViolationException ex) {
            // Unique email/subject constraints also protect concurrent first sign-ins.
            throw new OAuth2AuthenticationException(new OAuth2Error("account_unavailable"), ex);
        }
    }

    private static final class LocalGoogleUser extends DefaultOidcUser {
        private static final long serialVersionUID = 1L;
        private final String localEmail;

        private LocalGoogleUser(OidcUser identity, String localEmail) {
            super(List.of(new SimpleGrantedAuthority("ROLE_DONOR")), identity.getIdToken(), identity.getUserInfo());
            this.localEmail = localEmail;
        }

        @Override
        public String getName() {
            return localEmail;
        }
    }
}
