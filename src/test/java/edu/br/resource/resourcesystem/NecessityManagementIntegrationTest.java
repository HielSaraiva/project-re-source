package edu.br.resource.resourcesystem;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.br.resource.resourcesystem.dto.request.*;
import edu.br.resource.resourcesystem.model.entity.*;
import edu.br.resource.resourcesystem.model.enums.*;
import edu.br.resource.resourcesystem.repository.*;
import edu.br.resource.resourcesystem.service.match.*;
import edu.br.resource.resourcesystem.service.necessity.NecessityManagementService;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "resource.notifications.email-enabled=false")
@AutoConfigureMockMvc
class NecessityManagementIntegrationTest {
    @Autowired NecessityManagementService management;
    @Autowired MatchWorkflowService workflow;
    @Autowired MatchPageService pages;
    @Autowired InstitutionRepository institutions;
    @Autowired UserRepository users;
    @Autowired NecessityRepository needs;
    @Autowired DonationMatchRepository matches;
    @Autowired TransactionTemplate tx;
    @Autowired JdbcTemplate jdbc;
    @Autowired MockMvc mvc;
    private static final AtomicLong SEQ = new AtomicLong(900000);
    Institution owner;
    User donor;
    Integer needId;

    @BeforeEach
    void prepare() {
        long n = SEQ.incrementAndGet();
        owner =
                institutions.saveAndFlush(
                        Institution.builder()
                                .cnpj(String.format("%014d", n))
                                .representativeCpf(String.format("%011d", n))
                                .legalName("ONG teste " + n)
                                .email("ong" + n + "@example.org")
                                .passwordHash("test-only")
                                .representativeFullName("Responsável")
                                .status(InstitutionStatus.APPROVED)
                                .build());
        donor =
                users.saveAndFlush(
                        User.builder()
                                .fullName("Doador teste")
                                .email("donor" + n + "@example.org")
                                .passwordHash("test-only")
                                .build());
        management.create(owner.getId(), request("Item " + n, 10, null));
        needId =
                management
                        .list(
                                owner.getId(),
                                new NecessityFilterRequest(null, null, null, null, 1, 9))
                        .getContent()
                        .getFirst()
                        .id();
    }

    private ManageNecessityRequest request(String name, int quantity, Long version) {
        return new ManageNecessityRequest(
                ItemCategory.FURNITURE,
                name,
                BigDecimal.valueOf(quantity),
                NecessityPriority.HIGH,
                version);
    }

    private edu.br.resource.resourcesystem.dto.response.MatchDetailResponse proposal(int quantity) {
        var donation =
                workflow.register(
                        donor.getId(),
                        new RegisterDonationRequest(
                                needId, "Cadeiras", 10, ItemCondition.GOOD, null));
        return workflow.propose(
                donor.getId(), new SubmitProposalRequest(needId, donation.id(), quantity, null));
    }

    @Test
    void concurrentSpellingVariantsShareOneType() throws Exception {
        String name = "Tipo concorrente " + SEQ.incrementAndGet();
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var a =
                    executor.submit(
                            () -> {
                                start.await();
                                management.create(owner.getId(), request(name, 2, null));
                                return true;
                            });
            var b =
                    executor.submit(
                            () -> {
                                start.await();
                                management.create(
                                        owner.getId(),
                                        request(name.toUpperCase(Locale.ROOT), 2, null));
                                return true;
                            });
            start.countDown();
            assertTrue(a.get(15, TimeUnit.SECONDS));
            assertTrue(b.get(15, TimeUnit.SECONDS));
        }
        assertEquals(
                1,
                jdbc.queryForObject(
                        "select count(*) from item_types where lower(btrim(name))=lower(?)",
                        Integer.class,
                        name));
        management.create(owner.getId(), request(name, 1, null));
    }

    @ParameterizedTest
    @EnumSource(
            value = MatchStatus.class,
            names = {"CANCELLED", "REJECTED"},
            mode = EnumSource.Mode.EXCLUDE)
    void everyActiveOrCompletedStatusBlocksBothActions(MatchStatus status) {
        var proposal = proposal(4);
        tx.executeWithoutResult(
                s -> {
                    var m = matches.findById(proposal.id()).orElseThrow();
                    m.setStatus(status);
                    matches.flush();
                });
        var details = management.editDetails(owner.getId(), needId);
        assertFalse(details.editable());
        assertFalse(details.deletable());
        if (status == MatchStatus.COMPLETED) {
            assertTrue(details.restrictionReason().contains("propostas concluídas"));
            assertFalse(details.restrictionReason().contains("em andamento"));
        } else {
            assertTrue(details.restrictionReason().contains("em andamento"));
            assertFalse(details.restrictionReason().contains("concluídas"));
        }
        assertEquals(status == MatchStatus.COMPLETED ? 4 : 0, details.receivedQuantity());
        assertEquals(
                MatchStatus.activeStatuses().contains(status) ? 4 : 0, details.reservedQuantity());
        assertEquals(
                409,
                assertThrows(
                                org.springframework.web.server.ResponseStatusException.class,
                                () ->
                                        management.update(
                                                owner.getId(),
                                                needId,
                                                request("Alterado", 20, details.version())))
                        .getStatusCode()
                        .value());
        assertEquals(
                409,
                assertThrows(
                                org.springframework.web.server.ResponseStatusException.class,
                                () -> management.delete(owner.getId(), needId, details.version()))
                        .getStatusCode()
                        .value());
        assertEquals(10, needs.findById(needId).orElseThrow().getQuantityRequested());
    }

    @ParameterizedTest
    @EnumSource(
            value = MatchStatus.class,
            names = {"CANCELLED", "REJECTED"})
    void closedUnfulfilledProposalsAllowEditingAndDeletionWithoutDeletingHistory(
            MatchStatus status) {
        var proposal = proposal(4);
        if (status == MatchStatus.CANCELLED) workflow.cancel(donor.getId(), proposal.protocol());
        else workflow.reject(owner.getId(), proposal.protocol(), "Não atende ao uso pretendido");
        var details = management.editDetails(owner.getId(), needId);
        assertTrue(details.editable());
        assertTrue(details.deletable());
        assertEquals(0, details.minimumQuantity());
        var changed =
                management.update(
                        owner.getId(), needId, request("Novo nome", 2, details.version()));
        management.delete(owner.getId(), needId, changed.version());
        assertNotNull(needs.findById(needId).orElseThrow().getDeletedAt());
        assertTrue(matches.findById(proposal.id()).isPresent());
        assertEquals(
                1,
                jdbc.queryForObject(
                        "select count(*) from match_status_history where match_id=? and"
                                + " new_status::text=?",
                        Integer.class,
                        proposal.id(),
                        status.getValue()));
        assertTrue(
                management
                        .list(
                                owner.getId(),
                                new NecessityFilterRequest(null, null, null, null, 1, 9))
                        .isEmpty());
        assertTrue(needs.findByIdAndStatus(needId, NecessityStatus.ACTIVE).isEmpty());
        assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> management.editDetails(owner.getId(), needId));
        assertThrows(
                org.springframework.web.server.ResponseStatusException.class, () -> proposal(1));
    }

    @Test
    void anOlderFormCannotOverwriteAnotherEdit() {
        var before = management.editDetails(owner.getId(), needId);
        var first =
                management.update(
                        owner.getId(), needId, request("Primeira edição", 12, before.version()));
        assertTrue(first.version() > before.version());
        assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () ->
                        management.update(
                                owner.getId(),
                                needId,
                                request("Edição antiga", 15, before.version())));
        assertEquals("Primeira edição", management.editDetails(owner.getId(), needId).itemName());
    }

    @Test
    void deadlineOrderingStillWorksWhenFilteringByNeed() {
        var first = proposal(2);
        var second = proposal(2);
        jdbc.update(
                "update matches set acceptance_deadline=now()+interval '2 days' where id=?",
                first.id());
        jdbc.update(
                "update matches set acceptance_deadline=now()+interval '5 days' where id=?",
                second.id());
        var result =
                pages.institutionMatches(
                        owner.getId(),
                        new MatchFilterRequest(
                                null,
                                needId,
                                null,
                                edu.br.resource.resourcesystem.dto.MatchSortOrder.DEADLINE,
                                1,
                                6));
        var pagination =
                (edu.br.resource.resourcesystem.dto.response.PageResponse<?>)
                        result.get("pagination");
        var summary =
                (edu.br.resource.resourcesystem.dto.response.MatchSummaryResponse)
                        pagination.content().getFirst();
        assertEquals(first.protocol(), summary.protocol());
    }

    @Test
    void editingRacesWithProposalWithoutInvalidatingTheReservation() throws Exception {
        var donation =
                workflow.register(
                        donor.getId(),
                        new RegisterDonationRequest(
                                needId, "Cadeiras", 10, ItemCondition.GOOD, null));
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var edit =
                    executor.submit(
                            () -> {
                                start.await();
                                try {
                                    management.update(
                                            owner.getId(), needId, request("Editado", 5, 0L));
                                    return 200;
                                } catch (org.springframework.web.server.ResponseStatusException e) {
                                    return e.getStatusCode().value();
                                }
                            });
            var propose =
                    executor.submit(
                            () -> {
                                start.await();
                                try {
                                    workflow.propose(
                                            donor.getId(),
                                            new SubmitProposalRequest(
                                                    needId, donation.id(), 7, null));
                                    return 200;
                                } catch (org.springframework.web.server.ResponseStatusException e) {
                                    return e.getStatusCode().value();
                                }
                            });
            start.countDown();
            var statuses =
                    new ArrayList<>(
                            List.of(
                                    edit.get(15, TimeUnit.SECONDS),
                                    propose.get(15, TimeUnit.SECONDS)));
            Collections.sort(statuses);
            assertEquals(List.of(200, 409), statuses);
        }
        var after = management.editDetails(owner.getId(), needId);
        assertTrue(after.reservedQuantity() <= after.quantity());
    }

    @Test
    void mixedCancelledAndRejectedHistoryAlsoAllowsRemoval() {
        var cancelled = proposal(2);
        workflow.cancel(donor.getId(), cancelled.protocol());
        var rejected = proposal(3);
        workflow.reject(owner.getId(), rejected.protocol(), "Não atende ao uso pretendido");
        var details = management.editDetails(owner.getId(), needId);
        assertTrue(details.deletable());
        management.delete(owner.getId(), needId, details.version());
        assertTrue(matches.findById(cancelled.id()).isPresent());
        assertTrue(matches.findById(rejected.id()).isPresent());
    }

    @Test
    void deletedNeedRetainsHistoryAndDoesNotChangeAnotherSharedItem() {
        var original = management.editDetails(owner.getId(), needId);
        management.create(owner.getId(), request(original.itemName(), 3, null));
        var changed =
                management.update(
                        owner.getId(),
                        needId,
                        new ManageNecessityRequest(
                                ItemCategory.BEDDING,
                                "Cobertores",
                                BigDecimal.valueOf(4),
                                NecessityPriority.LOW,
                                original.version()));
        assertEquals(ItemCategory.BEDDING, changed.category());
        var other =
                management.list(
                        owner.getId(),
                        new NecessityFilterRequest(null, ItemCategory.FURNITURE, null, null, 1, 9));
        assertEquals(1, other.getTotalElements());
        assertEquals(original.itemName(), other.getContent().getFirst().itemName());
    }

    @Test
    void ownershipCsrfAndVersionAreEnforcedAtHttpBoundary() throws Exception {
        var auth = user(owner.getEmail()).roles("ONG");
        String payload =
                "{\"category\":\"furniture\",\"description\":\"Alterado\",\"quantity\":12,\"priority\":\"high\",\"version\":0}";
        mvc.perform(
                        put("/api/institution/necessities/" + needId)
                                .with(auth)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                .andExpect(status().isForbidden());
        mvc.perform(
                        put("/api/institution/necessities/" + needId)
                                .with(auth)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(1));
        mvc.perform(
                        put("/api/institution/necessities/" + needId)
                                .with(auth)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                .andExpect(status().isConflict());
        mvc.perform(
                        get("/api/institution/necessities/" + needId)
                                .with(user(donor.getEmail()).roles("DONOR")))
                .andExpect(status().isForbidden());
    }
}
