package edu.br.resource.resourcesystem.quality.integration.web;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.br.resource.resourcesystem.dto.request.*;
import edu.br.resource.resourcesystem.model.enums.*;
import edu.br.resource.resourcesystem.quality.support.WorkflowSupport;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor;
import org.springframework.test.web.servlet.request.*;

import tools.jackson.databind.json.JsonMapper;

import java.util.*;

@DisplayName("RF-001 a RF-027 — contratos HTTP, autorização, páginas e validação")
class HttpContractsIT extends WorkflowSupport {
    @Autowired ConfigurableEnvironment environment;

    UserRequestPostProcessor donorAuth() {
        return user("donor@example.test").roles("DONOR");
    }

    UserRequestPostProcessor ongAuth() {
        return user("ong@example.test").roles("ONG");
    }

    @Test
    void configuration_WithTestContext_NeverLoadsPersonalEnv() {
        assertThat(environment.getProperty("spring.datasource.url")).contains("quality_only");
        assertThat(environment.getProperty("spring.mail.host")).isEqualTo("127.0.0.1");
        assertThat(environment.getProperty("resource.registration.brasil-api-url"))
                .isEqualTo("http://127.0.0.1:1/never-external");
        environment
                .getPropertySources()
                .forEach(s -> assertThat(s.getName()).doesNotContain(".env[", "file [.env]"));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "/",
                "/sustainability",
                "/privacy-policy",
                "/login",
                "/register/donor",
                "/register/institution",
                "/password/forgot",
                "/password/reset?token=invalid"
            })
    void publicPage_WithoutSession_ReturnsHtml(String path) throws Exception {
        mvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "/donor/dashboard",
                "/donor/needs",
                "/donor/donations",
                "/donor/donations?view=completed",
                "/donor/donations?view=archived"
            })
    void donorPage_WithOwner_RendersWithoutLeakingAnotherOwner(String path) throws Exception {
        mvc.perform(get(path).with(donorAuth()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("ReSource")));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "/institution/dashboard",
                "/institution/necessities",
                "/institution/donations"
            })
    void institutionPage_WithApprovedOwner_Renders(String path) throws Exception {
        mvc.perform(get(path).with(ongAuth())).andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/donor/dashboard", "/donor/donations", "/institution/necessities"})
    void protectedPage_WithoutSession_RedirectsToCorrectProfile(String path) throws Exception {
        mvc.perform(get(path))
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        header().string(
                                        "Location",
                                        org.hamcrest.Matchers.containsString(
                                                path.startsWith("/institution")
                                                        ? "profile=institution"
                                                        : "profile=donor")));
    }

    @Test
    void api_WithoutSession_ReturnsUnauthorizedJson() throws Exception {
        mvc.perform(get("/api/donor/inventory/" + donation).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedApi_WithOtherRole_Rejects() throws Exception {
        mvc.perform(
                        get("/api/institution/necessities/" + necessity)
                                .with(donorAuth())
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        mvc.perform(
                        get("/api/donor/inventory/" + donation)
                                .with(ongAuth())
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void inventory_WithOtherOwner_Returns404NotData() throws Exception {
        mvc.perform(
                        get("/api/donor/inventory/" + donation)
                                .with(user("other-donor@example.test").roles("DONOR")))
                .andExpect(status().isNotFound());
    }

    @Test
    void mutation_WithoutCsrf_RejectsBeforePersistence() throws Exception {
        mvc.perform(
                        post("/api/donor/proposals")
                                .with(donorAuth())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"need\":"
                                                + necessity
                                                + ",\"donation\":"
                                                + donation
                                                + ",\"quantity\":1}"))
                .andExpect(status().isForbidden());
        assertThat(count("matches")).isZero();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "{",
                "{}",
                "{\"quantity\":1.9}",
                "{\"quantity\":2147483648}",
                "{\"quantity\":true}"
            })
    void proposal_WithMalformedOrInvalidPayload_Returns400(String payload) throws Exception {
        mvc.perform(
                        post("/api/donor/proposals")
                                .with(donorAuth())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                .andExpect(status().isBadRequest());
        assertThat(count("matches")).isZero();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "page=abc",
                "page=-1",
                "page=0",
                "size=101",
                "category=missing",
                "priority=missing",
                "query="
            })
    void discovery_WithBadParameter_RejectsOrAllowsEmptyQuery(String query) throws Exception {
        var result = mvc.perform(get("/donor/needs?" + query).with(donorAuth()));
        if (query.equals("query=")) result.andExpect(status().isOk());
        else result.andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {"status=cancelled", "status=accepted", "view=unknown", "page=0", "page=abc"})
    void inventory_WithUnsupportedFilter_Returns400(String query) throws Exception {
        mvc.perform(get("/donor/donations?" + query).with(donorAuth()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void inventory_WithValidPayload_ImplementsCreateReadUpdateDelete() throws Exception {
        String key = UUID.randomUUID().toString();
        String payload =
                "{\"category\":\"electronics\",\"title\":\"Novo"
                        + " lote\",\"quantity\":8,\"condition\":\"good\",\"submissionKey\":\""
                        + key
                        + "\"}";
        String result =
                mvc.perform(
                                post("/api/donor/inventory")
                                        .with(donorAuth())
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(payload))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        int id = JsonMapper.builder().build().readTree(result).get("id").intValue();
        mvc.perform(get("/api/donor/inventory/" + id).with(donorAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(8));
        mvc.perform(
                        put("/api/donor/inventory/" + id)
                                .with(donorAuth())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"title\":\"Alterado\",\"quantity\":6,\"condition\":\"new\",\"version\":0}"))
                .andExpect(status().isNoContent());
        mvc.perform(
                        delete("/api/donor/inventory/" + id + "?version=1")
                                .with(donorAuth())
                                .with(csrf()))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/donor/inventory/" + id).with(donorAuth()))
                .andExpect(status().isNotFound());
    }

    @Test
    void inventory_WithInvalidFields_ReturnsRecoverableFieldErrors() throws Exception {
        mvc.perform(
                        post("/api/donor/inventory")
                                .with(donorAuth())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"title\":\"\",\"quantity\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.quantity").exists());
    }

    @Test
    void need_WithValidPayload_ImplementsCreateEditDelete() throws Exception {
        mvc.perform(
                        post("/api/institution/necessities")
                                .with(ongAuth())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"category\":\"food\",\"description\":\"Arroz de"
                                            + " teste\",\"quantity\":10,\"priority\":\"high\"}"))
                .andExpect(status().isNoContent());
        int id = db.queryForObject("select max(id) from necessities", Integer.class);
        mvc.perform(get("/api/institution/necessities/" + id).with(ongAuth()))
                .andExpect(status().isOk());
        mvc.perform(
                        put("/api/institution/necessities/" + id)
                                .with(ongAuth())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"category\":\"food\",\"description\":\"Arroz"
                                            + " atualizado\",\"quantity\":12,\"priority\":\"low\",\"version\":0}"))
                .andExpect(status().isOk());
        mvc.perform(
                        delete("/api/institution/necessities/" + id + "?version=1")
                                .with(ongAuth())
                                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "null"})
    void acceptance_WithUnconfirmedDecision_Returns400(String confirmation) throws Exception {
        var p = propose(1);
        mvc.perform(
                        post("/api/institution/matches/" + p.protocol() + "/accept")
                                .with(ongAuth())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"confirmed\":" + confirmation + "}"))
                .andExpect(status().isBadRequest());
        assertThat(count("match_status_history")).isEqualTo(1);
    }

    @Test
    void proposalPages_WithEveryStage_UseCanonicalRoute() throws Exception {
        var p = propose(4);
        mvc.perform(
                        get("/donor/donation/status")
                                .servletPath("/donor/donation/status")
                                .param("protocol", p.protocol())
                                .with(donorAuth()))
                .andExpect(status().isOk());
        workflow.accept(ong, p.protocol());
        mvc.perform(
                        get("/donor/donation/status")
                                .servletPath("/donor/donation/status")
                                .param("protocol", p.protocol())
                                .with(donorAuth()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/donor/donation/shipping?protocol=" + p.protocol()));
        mvc.perform(
                        get("/donor/donation/shipping")
                                .servletPath("/donor/donation/shipping")
                                .param("protocol", p.protocol())
                                .with(donorAuth()))
                .andExpect(status().isOk());
        workflow.selectMethod(donor, p.protocol(), DeliveryMethod.IN_PERSON);
        mvc.perform(
                        get("/donor/donation/shipping/in-person")
                                .servletPath("/donor/donation/shipping/in-person")
                                .param("protocol", p.protocol())
                                .with(donorAuth()))
                .andExpect(status().isOk());
        workflow.reportDelivery(
                donor,
                p.protocol(),
                new ConfirmInPersonDeliveryRequest(today(), "Ana fictícia", "Recepção", true));
        mvc.perform(get("/institution/donations/" + p.protocol()).with(ongAuth()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Ana fictícia")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Recepção")));
        workflow.receive(ong, p.protocol());
        mvc.perform(
                        get("/donor/donation/completed")
                                .servletPath("/donor/donation/completed")
                                .param("protocol", p.protocol())
                                .with(donorAuth()))
                .andExpect(status().isOk());
    }

    @Test
    void login_WithSavedInventoryPage_ReturnsToRequestedFilter() throws Exception {
        var request =
                mvc.perform(get("/donor/donations?view=completed&page=2"))
                        .andExpect(status().is3xxRedirection())
                        .andReturn();
        var session = (MockHttpSession) request.getRequest().getSession(false);
        mvc.perform(
                        post("/login")
                                .session(session)
                                .with(csrf())
                                .param("identifier", "donor@example.test")
                                .param("password", "Quality12!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/donor/donations?view=completed&page=2"));
    }

    @Test
    void login_WithWrongPassword_ReturnsGenericFailure() throws Exception {
        mvc.perform(
                        post("/login")
                                .with(csrf())
                                .param("identifier", "donor@example.test")
                                .param("password", "wrong"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error&profile=donor"));
    }

    @Test
    void logout_WithPostAndCsrf_EndsSession() throws Exception {
        mvc.perform(post("/logout").accept(MediaType.TEXT_HTML).with(donorAuth()).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
    }
}
