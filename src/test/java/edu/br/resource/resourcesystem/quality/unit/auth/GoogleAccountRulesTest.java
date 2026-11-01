package edu.br.resource.resourcesystem.quality.unit.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import edu.br.resource.resourcesystem.model.entity.*;
import edu.br.resource.resourcesystem.model.enums.*;
import edu.br.resource.resourcesystem.repository.*;
import edu.br.resource.resourcesystem.service.auth.*;
import edu.br.resource.resourcesystem.service.mail.TransactionalMailService;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RN-001/RN-016 — identidade Google sem vinculação indevida")
class GoogleAccountRulesTest {
    @Mock UserRepository users;
    @Mock InstitutionRepository institutions;
    @Mock AccountEmailRegistry emails;
    @Mock TransactionalMailService mail;
    @Mock OidcUser identity;
    @InjectMocks GoogleAccountService sut;

    void verified() {
        lenient().when(identity.getEmailVerified()).thenReturn(true);
        lenient().when(identity.getSubject()).thenReturn("fictitious-subject");
        lenient().when(identity.getEmail()).thenReturn(" Google@EXAMPLE.TEST ");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void resolve_WithMissingSubject_Rejects(String sub) {
        verified();
        when(identity.getSubject()).thenReturn(sub);
        assertThatThrownBy(() -> sut.resolve(identity))
                .isInstanceOf(OAuth2AuthenticationException.class);
        verifyNoInteractions(users, mail);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void resolve_WithMissingEmail_Rejects(String email) {
        verified();
        when(identity.getEmail()).thenReturn(email);
        assertThatThrownBy(() -> sut.resolve(identity))
                .isInstanceOf(OAuth2AuthenticationException.class);
    }

    @Test
    void resolve_WithUnverifiedEmail_Rejects() {
        verified();
        when(identity.getEmailVerified()).thenReturn(false);
        assertThatThrownBy(() -> sut.resolve(identity))
                .isInstanceOf(OAuth2AuthenticationException.class);
    }

    @Test
    void resolve_WithOversizedIdentity_Rejects() {
        verified();
        when(identity.getSubject()).thenReturn("x".repeat(256));
        assertThatThrownBy(() -> sut.resolve(identity))
                .isInstanceOf(OAuth2AuthenticationException.class);
    }

    @Test
    void resolve_WithNewIdentity_CreatesDonorAndQueuesWelcome() {
        verified();
        when(identity.getFullName()).thenReturn("Nome de teste");
        when(users.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        var account = sut.resolve(identity);
        assertThat(account.getEmail()).isEqualTo("google@example.test");
        assertThat(account.getRole()).isEqualTo(AccountRole.DONOR);
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.getPasswordHash()).isNull();
        verify(emails).lock("google@example.test");
        verify(mail).welcome(account);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void resolve_WithMissingName_UsesEmail(String name) {
        verified();
        when(identity.getFullName()).thenReturn(name);
        when(users.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(sut.resolve(identity).getFullName()).isEqualTo("google@example.test");
    }

    @Test
    void resolve_WithLongName_TruncatesTo200() {
        verified();
        when(identity.getFullName()).thenReturn("N".repeat(201));
        when(users.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(sut.resolve(identity).getFullName()).hasSize(200);
    }

    @Test
    void resolve_WithExistingLocalEmail_DoesNotLink() {
        verified();
        when(users.findByEmailIgnoreCase("google@example.test"))
                .thenReturn(Optional.of(User.builder().id(7).build()));
        assertThatThrownBy(() -> sut.resolve(identity))
                .isInstanceOf(OAuth2AuthenticationException.class);
        verify(users, never()).saveAndFlush(any());
    }

    @Test
    void resolve_WithInstitutionEmail_Rejects() {
        verified();
        when(institutions.findByEmailIgnoreCase("google@example.test"))
                .thenReturn(Optional.of(Institution.builder().build()));
        assertThatThrownBy(() -> sut.resolve(identity))
                .isInstanceOf(OAuth2AuthenticationException.class);
    }

    @ParameterizedTest
    @EnumSource(AccountStatus.class)
    void resolve_WithLinkedAccount_RespectsStatus(AccountStatus state) {
        verified();
        var account = User.builder().id(1).email("google@example.test").status(state).build();
        when(users.findByOauthProviderAndOauthSubject(any(), any()))
                .thenReturn(Optional.of(account));
        if (state == AccountStatus.ACTIVE) assertThat(sut.resolve(identity)).isSameAs(account);
        else
            assertThatThrownBy(() -> sut.resolve(identity))
                    .isInstanceOf(OAuth2AuthenticationException.class);
        verifyNoInteractions(mail);
    }
}
