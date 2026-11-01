package edu.br.resource.resourcesystem;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.br.resource.resourcesystem.dto.request.*;
import edu.br.resource.resourcesystem.model.entity.*;
import edu.br.resource.resourcesystem.model.enums.*;
import edu.br.resource.resourcesystem.repository.*;
import edu.br.resource.resourcesystem.service.*;
import edu.br.resource.resourcesystem.service.match.MatchWorkflowService;
import edu.br.resource.resourcesystem.service.necessity.NecessityManagementService;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "resource.notifications.email-enabled=false")
@AutoConfigureMockMvc
class DonorInventoryIntegrationTest {
    @Autowired DonationManagementService management;
    @Autowired DonationHistoryService history;
    @Autowired NecessityManagementService needs;
    @Autowired MatchWorkflowService workflow;
    @Autowired InstitutionRepository institutions;
    @Autowired UserRepository users;
    @Autowired DonationRepository donations;
    @Autowired DonationMatchRepository matches;
    @Autowired JdbcTemplate db;
    @Autowired MockMvc mvc;
    @Autowired TransactionTemplate tx;
    static final AtomicLong SEQ = new AtomicLong(70000000);
    User donor, other;
    Institution ong;
    Integer needId;

    @BeforeEach
    void fixture() {
        long n = SEQ.incrementAndGet();
        donor =
                users.saveAndFlush(
                        User.builder()
                                .fullName("Doador inventário")
                                .email("inventory" + n + "@example.org")
                                .passwordHash("test-only")
                                .build());
        other =
                users.saveAndFlush(
                        User.builder()
                                .fullName("Outro doador")
                                .email("other" + n + "@example.org")
                                .passwordHash("test-only")
                                .build());
        ong =
                institutions.saveAndFlush(
                        Institution.builder()
                                .cnpj(String.format("%014d", n))
                                .representativeCpf(String.format("%011d", n))
                                .legalName("ONG inventário")
                                .email("ong-inventory" + n + "@example.org")
                                .passwordHash("test-only")
                                .representativeFullName("Responsável")
                                .status(InstitutionStatus.APPROVED)
                                .build());
        needs.create(
                ong.getId(),
                new ManageNecessityRequest(
                        ItemCategory.FURNITURE, "Cadeiras " + n, 100, NecessityPriority.HIGH));
        needId =
                needs.list(ong.getId(), new NecessityFilterRequest(null, null, null, null, 1, 9))
                        .getContent()
                        .getFirst()
                        .id();
    }

    NewDonationForm createForm(String title) {
        var f = new NewDonationForm();
        f.setCategory(ItemCategory.FURNITURE);
        f.setTitle(title);
        f.setQuantity(10);
        f.setCondition(ItemCondition.GOOD);
        f.setDescription("Descrição inicial");
        f.setSubmissionKey(UUID.randomUUID());
        return f;
    }

    DonationEditForm editForm(String title, int quantity, long version) {
        var f = new DonationEditForm();
        f.setTitle(title);
        f.setQuantity(quantity);
        f.setVersion(version);
        f.setCondition(ItemCondition.USED);
        f.setDescription("Descrição editada");
        return f;
    }

    Integer create() {
        return management.register(donor.getId(), createForm("Inventário teste"));
    }

    edu.br.resource.resourcesystem.dto.response.MatchDetailResponse propose(
            Integer id, int quantity) {
        return workflow.propose(
                donor.getId(), new SubmitProposalRequest(needId, id, quantity, null));
    }

    @Test
    void concurrentRetryCreatesOneAggregateAndOneCreationHistory() throws Exception {
        var f = createForm("Reenvio concorrente");
        var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var a =
                    pool.submit(
                            () -> {
                                start.await();
                                return management.register(donor.getId(), f);
                            });
            var b =
                    pool.submit(
                            () -> {
                                start.await();
                                return management.register(donor.getId(), f);
                            });
            start.countDown();
            Integer id = a.get(15, TimeUnit.SECONDS);
            assertEquals(id, b.get(15, TimeUnit.SECONDS));
            assertEquals(
                    1,
                    db.queryForObject(
                            "select count(*) from donations where donor_user_id=?",
                            Integer.class,
                            donor.getId()));
            assertEquals(
                    1,
                    db.queryForObject(
                            "select count(*) from donation_status_history where donation_id=?",
                            Integer.class,
                            id));
        }
    }

    @Test
    void reusedKeyWithChangedPayloadDoesNotCreateMoreInventory() {
        var form = createForm("Primeira tentativa");
        var id = management.register(donor.getId(), form);
        form.setQuantity(20);
        assertThrows(ResponseStatusException.class, () -> management.register(donor.getId(), form));
        assertEquals(10, history.details(donor.getId(), id).quantity());
    }

    @Test
    void retryAfterDeletionDoesNotRecreateDeletedStock() {
        var f = createForm("Cadastro excluído");
        var id = management.register(donor.getId(), f);
        management.delete(donor.getId(), id, 0);
        assertThrows(ResponseStatusException.class, () -> management.register(donor.getId(), f));
        assertFalse(donations.existsById(id));
    }

    @Test
    void childChangesIncrementAggregateVersionAndRejectOldTab() {
        var id = create();
        management.update(donor.getId(), id, editForm("Nome da aba A", 10, 0));
        var updated = history.details(donor.getId(), id);
        assertEquals(1, updated.version());
        assertEquals("Nome da aba A", updated.itemName());
        assertEquals("Descrição editada", updated.description());
        assertThrows(
                ResponseStatusException.class,
                () -> management.update(donor.getId(), id, editForm("Nome antigo", 12, 0)));
        assertThrows(ResponseStatusException.class, () -> management.delete(donor.getId(), id, 0));
        assertEquals("Nome da aba A", history.details(donor.getId(), id).itemName());
    }

    @Test
    void allCancelledRejectedAndActiveProtocolsRemainNavigableAndPaged() {
        var id = create();
        for (int i = 0; i < 10; i++) {
            var m = propose(id, 1);
            if (i % 2 == 0) workflow.cancel(donor.getId(), m.protocol());
            else workflow.reject(ong.getId(), m.protocol(), "Não se aplica");
        }
        var active = propose(id, 2);
        var details = history.details(donor.getId(), id);
        assertEquals(11, details.proposalCount());
        assertEquals(1, details.activeProposals());
        assertEquals(2, details.reservedQuantity());
        assertEquals(8, details.availableQuantity());
        var first = history.proposals(donor.getId(), id, 1);
        var second = history.proposals(donor.getId(), id, 2);
        assertEquals(11, first.totalElements());
        assertEquals(8, first.content().size());
        assertEquals(3, second.content().size());
        assertEquals(active.protocol(), first.content().getFirst().protocol());
        assertTrue(first.content().stream().anyMatch(p -> p.status() == MatchStatus.REJECTED));
        assertTrue(first.content().stream().anyMatch(p -> p.status() == MatchStatus.CANCELLED));
        assertThrows(ResponseStatusException.class, () -> history.proposals(other.getId(), id, 1));
        assertThrows(
                ResponseStatusException.class,
                () -> management.update(donor.getId(), id, editForm("Bloqueado", 12, 0)));
        assertThrows(ResponseStatusException.class, () -> management.delete(donor.getId(), id, 0));
    }

    @Test
    void deletingOnlyCancelledAndRejectedPreservesProtocolsButHidesStock() {
        var form = createForm("Histórico excluído");
        var id = management.register(donor.getId(), form);
        var cancelled = propose(id, 2);
        workflow.cancel(donor.getId(), cancelled.protocol());
        var rejected = propose(id, 2);
        workflow.reject(ong.getId(), rejected.protocol(), "Não se aplica");
        assertTrue(history.details(donor.getId(), id).deletable());
        management.delete(donor.getId(), id, 0);
        assertNotNull(donations.findById(id).orElseThrow().getDeletedAt());
        assertTrue(
                history.findDonationsByDonor(
                                donor.getId(),
                                new DonationHistoryFilterRequest(null, null, null, 1))
                        .isEmpty());
        assertThrows(ResponseStatusException.class, () -> history.details(donor.getId(), id));
        var archived =
                history.findDonationsByDonor(
                        donor.getId(),
                        new DonationHistoryFilterRequest(null, null, null, 1, "archived"));
        assertEquals(1, archived.getTotalElements());
        assertTrue(archived.getContent().getFirst().archived());
        assertFalse(archived.getContent().getFirst().editable());
        assertFalse(archived.getContent().getFirst().deletable());
        assertTrue(
                history.findDonationsByDonor(
                                other.getId(),
                                new DonationHistoryFilterRequest(null, null, null, 1, "archived"))
                        .isEmpty());
        assertEquals(2, history.proposals(donor.getId(), id, 1).totalElements());
        assertEquals(
                1, history.proposals(donor.getId(), id, 1, MatchStatus.CANCELLED).totalElements());
        assertEquals(
                cancelled.protocol(),
                history.proposals(donor.getId(), id, 1, MatchStatus.CANCELLED)
                        .content()
                        .getFirst()
                        .protocol());
        assertTrue(matches.findById(cancelled.id()).isPresent());
        assertTrue(matches.findById(rejected.id()).isPresent());
        assertThrows(ResponseStatusException.class, () -> propose(id, 1));
        assertThrows(ResponseStatusException.class, () -> management.register(donor.getId(), form));
    }

    @Test
    void filtersAndPaginationPreserveOwnerAndFindHistoricalProtocols() {
        var id = create();
        var proposal = propose(id, 1);
        workflow.cancel(donor.getId(), proposal.protocol());
        management.register(other.getId(), createForm("Inventário de outra conta"));
        var byProtocol =
                history.findDonationsByDonor(
                        donor.getId(),
                        new DonationHistoryFilterRequest(proposal.protocol(), null, null, 1));
        assertEquals(1, byProtocol.getTotalElements());
        assertEquals(id, byProtocol.getContent().getFirst().id());
        assertTrue(
                history.findDonationsByDonor(
                                donor.getId(),
                                new DonationHistoryFilterRequest(null, ItemCategory.FOOD, null, 1))
                        .isEmpty());
        for (int i = 0; i < 9; i++) management.register(donor.getId(), createForm("Pagina " + i));
        var last =
                history.findDonationsByDonor(
                        donor.getId(),
                        new DonationHistoryFilterRequest(
                                null, null, DonationStatus.REGISTERED, 999));
        assertEquals(2, last.getNumber() + 1);
        assertEquals(1, last.getNumberOfElements());
        assertEquals(10, last.getTotalElements());
    }

    @Test
    void completedAllocationsKeepTheirFloorWhileInventoryWithBalanceCanBeEdited() {
        var id = create();
        var m = propose(id, 4);
        tx.executeWithoutResult(
                s -> {
                    matches.findById(m.id()).orElseThrow().setStatus(MatchStatus.COMPLETED);
                    var d = donations.findById(id).orElseThrow();
                    d.setStatus(DonationStatus.REGISTERED);
                });
        var details = history.details(donor.getId(), id);
        assertTrue(details.editable());
        assertFalse(details.deletable());
        assertEquals(4, details.receivedQuantity());
        assertThrows(
                ResponseStatusException.class,
                () -> management.update(donor.getId(), id, editForm("Inválido", 3, 0)));
        management.update(donor.getId(), id, editForm("Quantidade recebida", 4, 0));
        assertEquals(DonationStatus.COMPLETED, history.details(donor.getId(), id).status());
        assertFalse(history.details(donor.getId(), id).editable());
        var inventory =
                history.findDonationsByDonor(
                        donor.getId(), new DonationHistoryFilterRequest(null, null, null, 1));
        assertTrue(inventory.getContent().stream().noneMatch(d -> d.id().equals(id)));
        var completed =
                history.findDonationsByDonor(
                        donor.getId(),
                        new DonationHistoryFilterRequest(null, null, null, 1, "completed"));
        assertEquals(
                java.util.List.of(id), completed.getContent().stream().map(d -> d.id()).toList());
        assertTrue(
                history.findDonationsByDonor(
                                other.getId(),
                                new DonationHistoryFilterRequest(null, null, null, 1, "completed"))
                        .isEmpty());
    }

    @Test
    void jsonBoundaryKeepsValidationRecoverableAndChecksOwnerAndCsrf() throws Exception {
        String payload =
                "{\"category\":\"furniture\",\"title\":\"Cadastro"
                        + " HTTP\",\"quantity\":10,\"condition\":\"good\",\"submissionKey\":\""
                        + UUID.randomUUID()
                        + "\"}";
        var auth = user(donor.getEmail()).roles("DONOR");
        mvc.perform(
                        post("/api/donor/inventory")
                                .with(auth)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                .andExpect(status().isForbidden());
        mvc.perform(
                        post("/api/donor/inventory")
                                .with(auth)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload.replace("\"quantity\":10", "\"quantity\":0")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.quantity").exists());
        mvc.perform(
                        post("/api/donor/inventory")
                                .with(auth)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload.replace("\"quantity\":10", "\"quantity\":1.5")))
                .andExpect(status().isBadRequest());
        mvc.perform(
                        post("/api/donor/inventory")
                                .with(auth)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                .andExpect(status().isCreated());
        var id = create();
        mvc.perform(get("/api/donor/inventory/" + id).with(user(other.getEmail()).roles("DONOR")))
                .andExpect(status().isNotFound());
        mvc.perform(
                        get("/api/donor/inventory/" + id + "/proposals")
                                .with(user(other.getEmail()).roles("DONOR")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/donor/inventory/" + id).with(user(ong.getEmail()).roles("ONG")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/donor/donations").with(auth))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Doação #" + id)));
        mvc.perform(get("/donor/donations?page=abc").with(auth)).andExpect(status().isBadRequest());
        mvc.perform(get("/donor/donations?status=cancelled").with(auth))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/donor/donations?status=accepted").with(auth))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/donor/donations?view=archived").with(auth)).andExpect(status().isOk());
        mvc.perform(get("/donor/donations?view=completed").with(auth)).andExpect(status().isOk());
        mvc.perform(get("/donor/donations?view=invalid").with(auth))
                .andExpect(status().isBadRequest());
    }
}
