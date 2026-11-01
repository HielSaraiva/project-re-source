package edu.br.resource.resourcesystem.quality.unit.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import edu.br.resource.resourcesystem.model.entity.*;
import edu.br.resource.resourcesystem.model.enums.*;
import edu.br.resource.resourcesystem.repository.*;
import edu.br.resource.resourcesystem.security.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RN-001 — autenticação e isolamento por perfil")
class AuthenticationRulesTest {
    @Mock UserRepository users;
    @Mock InstitutionRepository institutions;
    @InjectMocks DatabaseUserDetailsService login;

    UsernamePasswordAuthenticationToken auth(String name, String role) {
        return new UsernamePasswordAuthenticationToken(
                name, "ignored", AuthorityUtils.createAuthorityList(role));
    }

    @Nested
    @DisplayName("Credenciais locais")
    class Local {
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t"})
        void login_WithMissingIdentifier_RejectsWithoutQuery(String value) {
            assertThatThrownBy(() -> login.loadUserByUsername(value))
                    .isInstanceOf(UsernameNotFoundException.class);
            verifyNoInteractions(users, institutions);
        }

        @ParameterizedTest
        @EnumSource(AccountStatus.class)
        void login_WithDonorStatus_EnablesOnlyActiveAccount(AccountStatus status) {
            when(users.findByEmailIgnoreCase("donor@example.test"))
                    .thenReturn(
                            Optional.of(
                                    User.builder()
                                            .email("donor@example.test")
                                            .passwordHash("$fakehash")
                                            .status(status)
                                            .build()));
            var sut = login.loadUserByUsername(" donor@example.test ");
            assertThat(sut.isEnabled()).isEqualTo(status == AccountStatus.ACTIVE);
            assertThat(sut.getPassword()).isEqualTo("{bcrypt}$fakehash");
        }

        @ParameterizedTest
        @EnumSource(InstitutionStatus.class)
        void login_WithInstitutionStatus_EnablesOnlyApprovedAccount(InstitutionStatus status) {
            when(institutions.findByCnpj("99000000000101"))
                    .thenReturn(
                            Optional.of(
                                    Institution.builder()
                                            .email("ong@example.test")
                                            .passwordHash("{noop}fictitious")
                                            .status(status)
                                            .build()));
            var sut = login.loadUserByUsername("99.000.000/0001-01");
            assertThat(sut.isEnabled()).isEqualTo(status == InstitutionStatus.APPROVED);
            assertThat(sut.getAuthorities()).extracting("authority").containsExactly("ROLE_ONG");
        }

        @Test
        void login_WithUnknownAccount_Rejects() {
            assertThatThrownBy(() -> login.loadUserByUsername("unknown@example.test"))
                    .isInstanceOf(UsernameNotFoundException.class);
        }

        @Test
        void login_WithAmbiguousEmail_Rejects() {
            when(users.findByEmailIgnoreCase(anyString()))
                    .thenReturn(Optional.of(User.builder().build()));
            when(institutions.findByEmailIgnoreCase(anyString()))
                    .thenReturn(Optional.of(Institution.builder().build()));
            assertThatThrownBy(() -> login.loadUserByUsername("ambiguous@example.test"))
                    .isInstanceOf(UsernameNotFoundException.class);
        }

        @Test
        void login_WithGoogleOnlyAccount_RejectsLocalPassword() {
            when(users.findByEmailIgnoreCase(anyString()))
                    .thenReturn(Optional.of(User.builder().build()));
            assertThatThrownBy(() -> login.loadUserByUsername("google@example.test"))
                    .isInstanceOf(UsernameNotFoundException.class);
        }

        @Test
        void login_WithAdministrator_UsesInternalRole() {
            when(users.findByEmailIgnoreCase(anyString()))
                    .thenReturn(
                            Optional.of(
                                    User.builder()
                                            .email("admin@example.test")
                                            .passwordHash("{noop}fake")
                                            .role(AccountRole.ADMINISTRATOR)
                                            .build()));
            assertThat(login.loadUserByUsername("admin@example.test").getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_ADMINISTRATOR");
        }
    }

    @Nested
    @DisplayName("Ator da operação")
    class Actor {
        @ParameterizedTest
        @ValueSource(strings = {"ROLE_ONG", "ROLE_ADMINISTRATOR", "ROLE_ANONYMOUS"})
        void donor_WithWrongRole_RejectsBeforeReadingAccount(String role) {
            assertThatThrownBy(
                            () -> new CurrentActor(users, institutions).donor(auth("fake", role)))
                    .isInstanceOf(ResponseStatusException.class);
            verifyNoInteractions(users, institutions);
        }

        @Test
        void donor_WithNoSession_Rejects() {
            assertThatThrownBy(() -> new CurrentActor(users, institutions).donor(null))
                    .isInstanceOf(ResponseStatusException.class);
        }

        @ParameterizedTest
        @EnumSource(AccountStatus.class)
        void donor_WithStatus_ReturnsOnlyActiveDonor(AccountStatus status) {
            var u = User.builder().email("d@example.test").status(status).build();
            when(users.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(u));
            var sut = new CurrentActor(users, institutions);
            if (status == AccountStatus.ACTIVE)
                assertThat(sut.donor(auth(u.getEmail(), "ROLE_DONOR"))).isSameAs(u);
            else
                assertThatThrownBy(() -> sut.donor(auth(u.getEmail(), "ROLE_DONOR")))
                        .isInstanceOf(ResponseStatusException.class);
        }

        @ParameterizedTest
        @EnumSource(InstitutionStatus.class)
        void institution_WithStatus_ReturnsOnlyApproved(InstitutionStatus status) {
            var i = Institution.builder().email("i@example.test").status(status).build();
            when(institutions.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(i));
            var sut = new CurrentActor(users, institutions);
            if (status == InstitutionStatus.APPROVED)
                assertThat(sut.institution(auth(i.getEmail(), "ROLE_ONG"))).isSameAs(i);
            else
                assertThatThrownBy(() -> sut.institution(auth(i.getEmail(), "ROLE_ONG")))
                        .isInstanceOf(ResponseStatusException.class);
        }

        @Test
        void institution_WithNoSession_Rejects() {
            assertThatThrownBy(() -> new CurrentActor(users, institutions).institution(null))
                    .isInstanceOf(ResponseStatusException.class);
        }
    }

    @Nested
    @DisplayName("Retorno seguro após login")
    class ReturnUrl {
        @ParameterizedTest
        @ValueSource(
                strings = {
                    "/donor/dashboard",
                    "/donor/donations?view=completed&page=2",
                    "/donor/donation/status?protocol=INT-2026-1",
                    "/donor/needs?query=abc"
                })
        void destination_WithAllowedDonorRoute_PreservesQuery(String path) {
            assertThat(LoginDestinationPolicy.destination(path, "", auth("d", "ROLE_DONOR")))
                    .contains(path);
        }

        @ParameterizedTest
        @ValueSource(
                strings = {
                    "javascript:alert(1)",
                    "//evil.example/donor/needs",
                    "/admin",
                    "/donor/needs#fragment",
                    "/donor/needs?x=%0a",
                    "/donor/needs?x=%0D",
                    "https://user:pass@example.test/donor/needs",
                    "http:/donor/needs",
                    "\u0000"
                })
        void destination_WithUnsafeRoute_ReturnsEmpty(String path) {
            assertThat(LoginDestinationPolicy.destination(path, "", auth("d", "ROLE_DONOR")))
                    .isEmpty();
        }

        @Test
        void destination_WithOtherProfile_ReturnsEmpty() {
            assertThat(
                            LoginDestinationPolicy.destination(
                                    "/institution/necessities", "", auth("d", "ROLE_DONOR")))
                    .isEmpty();
        }

        @Test
        void destination_WithContextPath_RemovesOnlyContext() {
            assertThat(
                            LoginDestinationPolicy.destination(
                                    "https://example.test/app/donor/needs?x=1",
                                    "/app",
                                    auth("d", "ROLE_DONOR")))
                    .contains("/donor/needs?x=1");
        }

        @Test
        void destination_WithOversizedQuery_Rejects() {
            assertThat(
                            LoginDestinationPolicy.destination(
                                    "/donor/needs?q=" + "a".repeat(2050),
                                    "",
                                    auth("d", "ROLE_DONOR")))
                    .isEmpty();
        }

        @ParameterizedTest
        @CsvSource({
            "GET,/donor/needs,true",
            "POST,/donor/needs,false",
            "GET,/institution/donations,true",
            "GET,/public,false"
        })
        void cache_WithPageAndMethod_UsesOnlyProtectedGet(
                String method, String path, boolean expected) {
            var r = new MockHttpServletRequest(method, path);
            assertThat(LoginDestinationPolicy.cacheable(r)).isEqualTo(expected);
            assertThat(LoginDestinationPolicy.loginPath(r))
                    .contains(
                            path.startsWith("/institution/")
                                    ? "profile=institution"
                                    : "profile=donor");
        }
    }
}
