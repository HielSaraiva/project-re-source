package edu.br.resource.resourcesystem.quality.unit.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import edu.br.resource.resourcesystem.config.GoogleOAuthConfiguration;
import edu.br.resource.resourcesystem.model.entity.User;
import edu.br.resource.resourcesystem.security.GoogleOidcUserService;
import edu.br.resource.resourcesystem.service.auth.GoogleAccountService;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.oidc.userinfo.*;
import org.springframework.security.oauth2.client.registration.*;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.oidc.*;
import org.springframework.security.oauth2.core.user.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.*;
import java.util.*;

@DisplayName("RN-001 — adaptação OIDC com identidade e endpoints inteiramente fictícios")
class GoogleOidcAdapterTest {
    GoogleAccountService accounts = mock(GoogleAccountService.class);
    GoogleOidcUserService sut = new GoogleOidcUserService(accounts);

    OidcUserRequest request(String provider) {
        var registration =
                ClientRegistration.withRegistrationId(provider)
                        .clientId("fictitious-client")
                        .clientSecret("fictitious-secret")
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .redirectUri("http://127.0.0.1/callback")
                        .authorizationUri("http://127.0.0.1/authorize")
                        .tokenUri("http://127.0.0.1/token")
                        .userInfoUri("http://127.0.0.1/userinfo")
                        .userNameAttributeName("sub")
                        .scope("openid", "profile", "email")
                        .build();
        var claims = new HashMap<String, Object>();
        claims.put("sub", "fictitious-subject");
        claims.put("email", "identity@example.test");
        claims.put("email_verified", true);
        claims.put("name", "Pessoa fictícia");
        var token =
                new OidcIdToken(
                        "not-a-real-token",
                        Instant.now().minusSeconds(1),
                        Instant.now().plusSeconds(600),
                        claims);
        var delegate = (OidcUserService) ReflectionTestUtils.getField(sut, "delegate");
        delegate.setOauth2UserService(
                r ->
                        new DefaultOAuth2User(
                                AuthorityUtils.createAuthorityList("OIDC_USER"), claims, "sub"));
        return new OidcUserRequest(
                registration,
                new OAuth2AccessToken(
                        OAuth2AccessToken.TokenType.BEARER,
                        "fake-access-token",
                        Instant.now().minusSeconds(1),
                        Instant.now().plusSeconds(600),
                        Set.of("openid", "profile", "email")),
                token);
    }

    @Test
    void load_WithUnsupportedProvider_RejectsBeforeResolvingAccount() {
        assertThatThrownBy(() -> sut.loadUser(request("other")))
                .isInstanceOf(OAuth2AuthenticationException.class);
        verifyNoInteractions(accounts);
    }

    @Test
    void load_WithResolvedAccount_UsesLocalEmailAsSessionIdentity() {
        when(accounts.resolve(any()))
                .thenReturn(User.builder().email("local@example.test").build());
        var result = sut.loadUser(request("google"));
        assertThat(result.getName()).isEqualTo("local@example.test");
        assertThat(result.getAuthorities()).extracting("authority").containsExactly("ROLE_DONOR");
    }

    @Test
    void load_WithDatabaseCollision_RejectsWithoutLeakingDatabaseDetails() {
        when(accounts.resolve(any()))
                .thenThrow(new DataIntegrityViolationException("fictitious-private-detail"));
        assertThatThrownBy(() -> sut.loadUser(request("google")))
                .isInstanceOfSatisfying(
                        OAuth2AuthenticationException.class,
                        e ->
                                assertThat(e.getError().getErrorCode())
                                        .isEqualTo("account_unavailable"));
    }

    @Test
    void configure_WithFakeCredentials_OnlyBuildsProviderMetadata() {
        new ApplicationContextRunner()
                .withUserConfiguration(GoogleOAuthConfiguration.class)
                .withPropertyValues(
                        "resource.auth.google.enabled=true",
                        "resource.auth.google.client-id=fake-client",
                        "resource.auth.google.client-secret=fake-secret")
                .run(
                        context -> {
                            var registration =
                                    context.getBean(ClientRegistrationRepository.class)
                                            .findByRegistrationId("google");
                            assertThat(registration.getClientId()).isEqualTo("fake-client");
                            assertThat(registration.getScopes())
                                    .contains("openid", "profile", "email");
                        });
    }

    @Test
    void configure_WithoutCredentials_RejectsEnabledIntegration() {
        new ApplicationContextRunner()
                .withUserConfiguration(GoogleOAuthConfiguration.class)
                .withPropertyValues(
                        "resource.auth.google.enabled=true",
                        "resource.auth.google.client-id=",
                        "resource.auth.google.client-secret=")
                .run(context -> assertThat(context).hasFailed());
    }
}
