package edu.br.resource.resourcesystem.quality.integration.web;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.br.resource.resourcesystem.dto.request.*;
import edu.br.resource.resourcesystem.dto.response.*;
import edu.br.resource.resourcesystem.model.enums.*;
import edu.br.resource.resourcesystem.quality.support.WorkflowSupport;
import edu.br.resource.resourcesystem.service.auth.CnpjLookupService;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.*;

import java.util.*;

@DisplayName("RF-004 a RF-024 — jornadas HTTP completas e apresentação por etapa")
class HttpLifecycleIT extends WorkflowSupport {
    @Autowired tools.jackson.databind.ObjectMapper json;

    String postJson(String path, Object body, boolean institution) throws Exception {
        return mvc.perform(
                        post(path)
                                .with(
                                        user(institution
                                                        ? "ong@example.test"
                                                        : "donor@example.test")
                                                .roles(institution ? "ONG" : "DONOR"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @ParameterizedTest
    @EnumSource(
            value = DeliveryMethod.class,
            names = {"IN_PERSON", "CARRIER"})
    void donation_WithFullHttpJourney_ConcludesWithoutDoubleReceipt(DeliveryMethod method)
            throws Exception {
        String registered =
                postJson(
                        "/api/donor/donations",
                        new RegisterDonationRequest(
                                necessity,
                                "Lote HTTP",
                                10,
                                ItemCondition.GOOD,
                                "Detalhes HTTP",
                                UUID.randomUUID()),
                        false);
        int id = json.readTree(registered).get("id").intValue();
        String proposed =
                postJson(
                        "/api/donor/proposals",
                        new SubmitProposalRequest(necessity, id, 10, "Doação HTTP"),
                        false);
        String protocol = json.readTree(proposed).get("protocol").asString();
        postJson(
                "/api/institution/matches/" + protocol + "/accept",
                new AcceptProposalRequest(true),
                true);
        postJson(
                "/api/donor/matches/" + protocol + "/method",
                new SelectDeliveryMethodRequest(method, true),
                false);
        if (method == DeliveryMethod.IN_PERSON)
            postJson(
                    "/api/donor/matches/" + protocol + "/in-person",
                    new ConfirmInPersonDeliveryRequest(
                            today(), "Pessoa de teste", "Observação HTTP", true),
                    false);
        else
            postJson(
                    "/api/donor/matches/" + protocol + "/shipment",
                    new ConfirmCarrierShipmentRequest(
                            today(), "AB123456789BR", "Observação HTTP", true),
                    false);
        postJson(
                "/api/institution/matches/" + protocol + "/receipt",
                new ConfirmReceiptRequest(true),
                true);
        mvc.perform(
                        post("/api/institution/matches/" + protocol + "/receipt")
                                .with(user("ong@example.test").roles("ONG"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"receiptConfirmed\":true}"))
                .andExpect(status().isConflict());
        assertThat(
                        db.queryForObject(
                                "select status::text from matches where protocol=?",
                                String.class,
                                protocol))
                .isEqualTo("completed");
    }

    @ParameterizedTest
    @ValueSource(strings = {"cancel", "reject"})
    void proposal_WithHttpClosure_ReleasesReservation(String action) throws Exception {
        var p = propose(4);
        if (action.equals("cancel"))
            postJson(
                    "/api/donor/matches/" + p.protocol() + "/cancel",
                    new CancelDonationRequest(true),
                    false);
        else
            postJson(
                    "/api/institution/matches/" + p.protocol() + "/reject",
                    new RejectProposalRequest("Motivo informado"),
                    true);
        assertThat(inventoryView.details(donor, donation).availableQuantity()).isEqualTo(10);
    }

    @ParameterizedTest
    @EnumSource(Stage.class)
    void detailPages_WithEachBusinessStage_RenderCanonicalViewAndHistory(Stage stage)
            throws Exception {
        var match = stage(stage);
        String path = "/donor/donation/" + pages.canonicalPath(match);
        mvc.perform(
                        get(path)
                                .servletPath(path)
                                .param("protocol", match.protocol())
                                .with(user("donor@example.test").roles("DONOR")))
                .andExpect(status().isOk())
                .andExpect(
                        content().string(org.hamcrest.Matchers.containsString(match.protocol())));
        mvc.perform(
                        get("/institution/donations/" + match.protocol())
                                .with(user("ong@example.test").roles("ONG")))
                .andExpect(status().isOk());
    }

    @Test
    void proposalPage_WithCompatibleInventory_PresentsRealAvailableStock() throws Exception {
        mvc.perform(
                        get("/donor/donation/proposal")
                                .param("need", String.valueOf(necessity))
                                .with(user("donor@example.test").roles("DONOR")))
                .andExpect(status().isOk())
                .andExpect(
                        content()
                                .string(
                                        org.hamcrest.Matchers.containsString(
                                                "Equipamento oferecido")));
    }

    @Test
    void inventoryProposals_WithOwner_ExposesPaginatedHistory() throws Exception {
        var p = propose(1);
        mvc.perform(
                        get("/api/donor/inventory/" + donation + "/proposals?page=999")
                                .with(user("donor@example.test").roles("DONOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].protocol").value(p.protocol()));
    }

    @Test
    void notifications_WithOwnReadAndReadAll_PreserveNoStore() throws Exception {
        propose(1);
        var auth = user("ong@example.test").roles("ONG");
        mvc.perform(get("/api/notifications").with(auth))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.unreadCount").value(1));
        UUID id = db.queryForObject("select id from user_notifications", UUID.class);
        mvc.perform(post("/api/notifications/" + id + "/read").with(auth).with(csrf()))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/notifications/read-all").with(auth).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @ParameterizedTest
    @CsvSource({
        "/sobre,/about",
        "/sustentabilidade,/sustainability",
        "/politica-de-privacidade,/privacy-policy",
        "/ong/dashboard,/institution/dashboard",
        "/ong/donations,/institution/donations",
        "/ong/donations/INT-2026-1,/institution/donations/INT-2026-1"
    })
    void legacyRoute_WithValidPath_RedirectsPermanently(String old, String current)
            throws Exception {
        mvc.perform(get(old).with(user("ong@example.test").roles("ONG")))
                .andExpect(status().isMovedPermanently())
                .andExpect(redirectedUrl(current));
    }

    @Test
    void registerInstitution_WithValidFakeCompanyAndFiles_PersistsPendingAccount()
            throws Exception {
        var pdf =
                "%PDF-1.7\nFictitious upload\n%%EOF"
                        .getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        mvc.perform(
                        multipart("/register/institution")
                                .file(
                                        new MockMultipartFile(
                                                "identityDocument",
                                                "identity.pdf",
                                                "application/pdf",
                                                pdf))
                                .file(
                                        new MockMultipartFile(
                                                "organizationDocument",
                                                "organization.pdf",
                                                "application/pdf",
                                                pdf))
                                .with(csrf())
                                .param("cnpj", "12ABC34501DE35")
                                .param("representativeCpf", "12345678909")
                                .param("representativeFullName", "Responsável fictício")
                                .param("email", "new-org@example.test")
                                .param("legalName", "Nome adulterado")
                                .param("password", "Quality12!")
                                .param("passwordConfirmation", "Quality12!")
                                .param("status", "approved"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?profile=institution&institutionRegistered"));
        assertThat(
                        db.queryForObject(
                                "select status::text from institutions where"
                                        + " email='new-org@example.test'",
                                String.class))
                .isEqualTo("pending_approval");
        assertThat(
                        db.queryForObject(
                                "select legal_name from institutions where"
                                        + " email='new-org@example.test'",
                                String.class))
                .isEqualTo("Organização fictícia");
    }

    @Test
    void registerInstitution_WithMissingFiles_RejectsWithoutPartialPersistence() throws Exception {
        mvc.perform(
                        multipart("/register/institution")
                                .with(csrf())
                                .param("cnpj", "12ABC34501DE35")
                                .param("representativeCpf", "12345678909")
                                .param("representativeFullName", "Pessoa fictícia")
                                .param("email", "missing-files@example.test")
                                .param("password", "Quality12!")
                                .param("passwordConfirmation", "Quality12!"))
                .andExpect(status().isOk())
                .andExpect(
                        model().attributeHasFieldErrors(
                                        "registration",
                                        "identityDocument",
                                        "organizationDocument"));
        assertThat(count("institutions")).isEqualTo(2);
    }

    @Test
    void company_WithUnavailableExternalStub_ReturnsSafeProblem() throws Exception {
        when(companyLookup.lookup(anyString()))
                .thenThrow(
                        new CnpjLookupService.LookupException(
                                HttpStatus.SERVICE_UNAVAILABLE, "Consulta indisponível"));
        mvc.perform(get("/register/institution/company").param("cnpj", "12ABC34501DE35"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.detail").value("Consulta indisponível"));
    }

    @Test
    void company_WithLocalStub_ReturnsCompany() throws Exception {
        mvc.perform(get("/register/institution/company").param("cnpj", "12ABC34501DE35"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.legalName").value("Organização fictícia"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/login", "/register/donor", "/register/institution"})
    void authenticationPage_WithExistingSession_ReturnsOwnDashboard(String path) throws Exception {
        mvc.perform(get(path).with(user("donor@example.test").roles("DONOR")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/donor/dashboard"));
    }

    @Test
    void donorRegistration_WithDuplicateEmail_PreservesFeedbackAndClearsPassword()
            throws Exception {
        mvc.perform(
                        post("/register/donor")
                                .with(csrf())
                                .param("fullName", "Pessoa fictícia")
                                .param("email", "donor@example.test")
                                .param("password", "Quality12!")
                                .param("passwordConfirmation", "Quality12!"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("registration", "email"));
    }

    @Test
    void passwordReset_WithUnknownToken_RejectsWithoutPasswordChange() throws Exception {
        mvc.perform(
                        post("/password/reset")
                                .with(csrf())
                                .param("token", "X".repeat(43))
                                .param("password", "Changed12!")
                                .param("passwordConfirmation", "Changed12!"))
                .andExpect(status().isOk());
    }
}
