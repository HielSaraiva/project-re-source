package edu.br.resource.resourcesystem.quality.integration.query;

import static org.assertj.core.api.Assertions.*;

import edu.br.resource.resourcesystem.dto.*;
import edu.br.resource.resourcesystem.dto.request.*;
import edu.br.resource.resourcesystem.dto.response.*;
import edu.br.resource.resourcesystem.model.enums.*;
import edu.br.resource.resourcesystem.quality.support.WorkflowSupport;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.*;

@DisplayName("RN-002/004/014 e RF-017 — descoberta, filtros, saldo e ordenação")
class QueriesAndDiscoveryIT extends WorkflowSupport {
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> rows(Map<String, Object> model, String key) {
        return (List<Map<String, Object>>) model.get(key);
    }

    @Test
    void discovery_WithApprovedActiveNeed_ReturnsRealLocationAndPositiveBalance() {
        int state = db.queryForObject("select state_id from cities limit 1", Integer.class);
        var model =
                pages.needs(
                        donor,
                        new NeedFilterRequest(
                                "oficina",
                                ItemCategory.ELECTRONICS,
                                NecessityPriority.HIGH,
                                state,
                                1,
                                6));
        assertThat(rows(model, "needs")).hasSize(1);
        assertThat(rows(model, "needs").getFirst().get("id")).isEqualTo(necessity);
        assertThat(rows(model, "needs").getFirst().get("distance")).isNull();
        assertThat(
                        rows(
                                pages.needs(
                                        donor, new NeedFilterRequest(null, null, null, 999, 1, 6)),
                                "needs"))
                .isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"blocked", "pending_approval"})
    void discovery_WithUnapprovedInstitution_HidesNeed(String state) {
        db.update("update institutions set status=?::institution_status where id=?", state, ong);
        assertThat(
                        rows(
                                pages.needs(
                                        donor, new NeedFilterRequest(null, null, null, null, 1, 6)),
                                "needs"))
                .isEmpty();
    }

    @ParameterizedTest
    @EnumSource(
            value = NecessityStatus.class,
            names = {"FULFILLED", "CANCELLED"})
    void discovery_WithClosedNeed_HidesIt(NecessityStatus state) {
        db.update(
                "update necessities set status=?::necessity_status where id=?",
                state.getValue(),
                necessity);
        assertThat(
                        rows(
                                pages.needs(
                                        donor, new NeedFilterRequest(null, null, null, null, 1, 6)),
                                "needs"))
                .isEmpty();
    }

    @Test
    void discovery_WithCompletelyReservedNeed_HidesItUntilCancellation() {
        var p = propose(10);
        assertThat(
                        rows(
                                pages.needs(
                                        donor, new NeedFilterRequest(null, null, null, null, 1, 6)),
                                "needs"))
                .isEmpty();
        workflow.cancel(donor, p.protocol());
        assertThat(
                        rows(
                                pages.needs(
                                        donor, new NeedFilterRequest(null, null, null, null, 1, 6)),
                                "needs"))
                .hasSize(1);
    }

    @ParameterizedTest
    @EnumSource(MatchSortOrder.class)
    void institutionList_WithTextStatusNeedAndOutOfRange_PreservesAllFilters(MatchSortOrder order) {
        var p = propose(4);
        int second = needFor(otherOng, type(), 10);
        workflow.propose(donor, new SubmitProposalRequest(second, donation, 1, null));
        var result =
                pages.institutionMatches(
                        ong,
                        new MatchFilterRequest(
                                "Equipamento oferecido",
                                necessity,
                                MatchStatus.AWAITING_ACCEPTANCE,
                                order,
                                999,
                                6));
        var page = (PageResponse<?>) result.get("pagination");
        assertThat(page.page()).isEqualTo(1);
        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(rows(result, "intentions"))
                .singleElement()
                .satisfies(r -> assertThat(r.get("protocol")).isEqualTo(p.protocol()));
    }

    @Test
    void institutionList_WithBlankSearchAndDeadlineSort_ReturnsOnlyOwnMatches() {
        propose(2);
        assertThat(
                        rows(
                                pages.institutionMatches(
                                        otherOng,
                                        new MatchFilterRequest(
                                                " ", null, null, MatchSortOrder.DEADLINE, 1, 6)),
                                "intentions"))
                .isEmpty();
    }

    @Test
    void donorProposal_WithNoCompatibleStock_RendersEmptySelection() {
        db.update("update items set item_type_id=(select id from item_types where id=?)", type());
        inventory.delete(donor, donation, 0);
        assertThat(rows(pages.proposal(donor, necessity), "inventoryDonations")).isEmpty();
    }

    @Test
    void donorProposal_WithUnavailableNeed_Rejects() {
        db.update("update institutions set status='blocked' where id=?", ong);
        assertThatThrownBy(() -> pages.proposal(donor, necessity))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
    }

    @Test
    void inventorySearch_WithIdProtocolOrInstitution_FindsSameOwnedDonation() {
        var p = propose(1);
        for (String term : List.of("#" + donation, p.protocol(), "ong", "Funcionando"))
            assertThat(
                            inventoryView
                                    .findDonationsByDonor(
                                            donor,
                                            new DonationHistoryFilterRequest(term, null, null, 1))
                                    .getContent())
                    .extracting(DonationHistoryResponse::id)
                    .containsExactly(donation);
    }

    @Test
    void historicalSearch_AfterChangingRemainingInventory_FindsOriginalOfferedItem() {
        var p = stage(Stage.COMPLETED);
        inventory.update(donor, donation, edit(10, 0));
        for (MatchSortOrder order : MatchSortOrder.values()) {
            var result =
                    pages.institutionMatches(
                            ong,
                            new MatchFilterRequest(
                                    "Equipamento oferecido",
                                    necessity,
                                    MatchStatus.COMPLETED,
                                    order,
                                    1,
                                    6));
            assertThat(((PageResponse<?>) result.get("pagination")).totalElements()).isEqualTo(1);
        }
        assertThat(
                        ((MatchDetailResponse) pages.donorDetail(donor, p.protocol()).get("match"))
                                .donation()
                                .description())
                .isEqualTo("Funcionando, dado fictício");
    }

    @Test
    void proposalFilter_WithLegacyStates_GroupsByCurrentEquivalent() {
        var p = propose(1);
        db.update("update matches set status='proposed' where id=?", p.id());
        assertThat(
                        inventoryView
                                .proposals(donor, donation, 1, MatchStatus.AWAITING_ACCEPTANCE)
                                .totalElements())
                .isEqualTo(1);
        db.update("update matches set status='accepted' where id=?", p.id());
        assertThat(
                        inventoryView
                                .proposals(donor, donation, 1, MatchStatus.AWAITING_SHIPMENT)
                                .totalElements())
                .isEqualTo(1);
    }
}
