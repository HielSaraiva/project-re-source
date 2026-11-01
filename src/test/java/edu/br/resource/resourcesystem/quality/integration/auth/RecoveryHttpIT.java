package edu.br.resource.resourcesystem.quality.integration.auth;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.br.resource.resourcesystem.quality.support.QualityIntegrationSupport;

import jakarta.mail.*;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;

import java.time.*;
import java.util.regex.*;

@DisplayName("RF-020/RF-021 — recuperação HTTP com envio fictício após commit")
class RecoveryHttpIT extends QualityIntegrationSupport {
    String text(Part part) throws Exception {
        Object c = part.getContent();
        if (c instanceof String s) return s;
        if (c instanceof Multipart m) {
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < m.getCount(); i++) b.append(text(m.getBodyPart(i)));
            return b.toString();
        }
        return "";
    }

    @Test
    void recovery_WithFullHttpJourney_ChangesPasswordOnceAndClearsSecrets() throws Exception {
        createUser("http-reset");
        mvc.perform(post("/password/forgot").with(csrf()).param("email", "http-reset@example.test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/password/forgot?sent"));
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertThat(outgoing).hasSize(1));
        var matcher =
                Pattern.compile("/password/reset\\?token=([A-Za-z0-9_-]{43})")
                        .matcher(text(outgoing.getFirst()));
        assertThat(matcher.find()).isTrue();
        String token = matcher.group(1);
        mvc.perform(get("/password/reset").param("token", token))
                .andExpect(status().isOk())
                .andExpect(model().attribute("validToken", true))
                .andExpect(header().string("Cache-Control", "no-store"));
        mvc.perform(
                        post("/password/reset")
                                .with(csrf())
                                .param("token", token)
                                .param("password", "Changed12!")
                                .param("passwordConfirmation", "Changed12!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?passwordReset&profile=donor"));
        mvc.perform(get("/password/reset").param("token", token))
                .andExpect(status().isOk())
                .andExpect(model().attribute("validToken", false));
        assertThat(
                        encoder.matches(
                                "Changed12!",
                                db.queryForObject("select password_hash from users", String.class)))
                .isTrue();
    }

    @Test
    void recovery_WithInvalidEmail_ReturnsFieldErrorWithoutToken() throws Exception {
        mvc.perform(post("/password/forgot").with(csrf()).param("email", "invalid"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("recovery", "email"));
        assertThat(count("password_reset_tokens")).isZero();
    }

    @Test
    void reset_WithInvalidConfirmation_DoesNotPersist() throws Exception {
        mvc.perform(
                        post("/password/reset")
                                .with(csrf())
                                .param("token", "T".repeat(43))
                                .param("password", "Changed12!")
                                .param("passwordConfirmation", "Different1!"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("reset", "passwordConfirmation"));
        assertThat(count("transactional_mail_outbox")).isZero();
    }
}
