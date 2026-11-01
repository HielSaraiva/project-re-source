package edu.br.resource.resourcesystem.quality.unit.policy;

import static org.assertj.core.api.Assertions.*;

import edu.br.resource.resourcesystem.config.PasswordRecoveryProperties;
import edu.br.resource.resourcesystem.dto.response.PageResponse;
import edu.br.resource.resourcesystem.model.entity.*;
import edu.br.resource.resourcesystem.model.enums.*;
import edu.br.resource.resourcesystem.repository.specification.SearchPatterns;
import edu.br.resource.resourcesystem.service.match.DonationStatusPolicy;
import edu.br.resource.resourcesystem.validation.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.time.*;
import java.util.*;

@DisplayName("RN-014, RN-018 e RN-019 — políticas independentes de infraestrutura")
class BusinessPoliciesTest {
    @Nested
    @DisplayName("RN-018 — senha forte")
    class Passwords {
        @ParameterizedTest
        @ValueSource(strings = {"Abcd123!", "Abcdefgh123!", "Ábcde12!", "A1!bcdef"})
        @DisplayName("Dado senha forte nos limites, quando validada, então deve aceitar")
        void validate_WithStrongPassword_ReturnsTrue(String password) {
            assertThat(PasswordPolicy.validFormat(password)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(
                strings = {
                    "",
                    "a",
                    "Abcd12!",
                    "Abcdefghi123!",
                    "abcdef1!",
                    "ABCDEF1!",
                    "Abcdefg!",
                    "Abcdef12",
                    "Abcdef1 ",
                    "Abcdef1\t",
                    "Abcdef1\n"
                })
        @DisplayName("Dado senha fraca ou fora do limite, quando validada, então deve rejeitar")
        void validate_WithInvalidPassword_ReturnsFalse(String password) {
            assertThat(PasswordPolicy.validFormat(password)).isFalse();
        }

        @Test
        void validate_WithUtf8ByteBoundary_Respects72Bytes() {
            assertThat(PasswordPolicy.withinByteLimit("é".repeat(36))).isTrue();
            assertThat(PasswordPolicy.withinByteLimit("é".repeat(37))).isFalse();
        }
    }

    @Nested
    @DisplayName("RN-014 — estado consolidado depende das propostas e do saldo")
    class Consolidation {
        DonationMatch match(int id, MatchStatus status, Instant at, int quantity) {
            return DonationMatch.builder()
                    .id(id)
                    .status(status)
                    .createdAt(at)
                    .allocatedQuantity(quantity)
                    .build();
        }

        @ParameterizedTest
        @EnumSource(
                value = MatchStatus.class,
                names = {
                    "AWAITING_ACCEPTANCE",
                    "AWAITING_SHIPMENT",
                    "AWAITING_DELIVERY",
                    "IN_TRANSIT",
                    "AWAITING_NGO_CONFIRMATION",
                    "ACCEPTED",
                    "PROPOSED"
                })
        void consolidate_WithActiveProposal_UsesOldestStatus(MatchStatus state) {
            var m = match(1, state, Instant.EPOCH, 2);
            var expected =
                    state == MatchStatus.PROPOSED
                            ? DonationStatus.AWAITING_ACCEPTANCE
                            : DonationStatus.fromValue(state.getValue());
            assertThat(DonationStatusPolicy.consolidate(10, List.of(m))).isEqualTo(expected);
        }

        @ParameterizedTest
        @EnumSource(
                value = MatchStatus.class,
                names = {"CANCELLED", "REJECTED"})
        void consolidate_WithReleasedProposal_RestoresRegistered(MatchStatus state) {
            var all = List.of(match(1, state, Instant.EPOCH, 10));
            assertThat(DonationStatusPolicy.consolidate(10, all))
                    .isEqualTo(DonationStatus.REGISTERED);
            assertThat(DonationStatusPolicy.allocatedQuantity(all)).isZero();
        }

        @Test
        void consolidate_WithPartialCompletion_KeepsRemainingStock() {
            var all = List.of(match(1, MatchStatus.COMPLETED, Instant.EPOCH, 4));
            assertThat(DonationStatusPolicy.consolidate(10, all))
                    .isEqualTo(DonationStatus.REGISTERED);
            assertThat(DonationStatusPolicy.consolidate(4, all))
                    .isEqualTo(DonationStatus.COMPLETED);
        }

        @Test
        void consolidate_WithNoProposals_ReturnsRegistered() {
            assertThat(DonationStatusPolicy.consolidate(1, List.of()))
                    .isEqualTo(DonationStatus.REGISTERED);
            assertThat(DonationStatusPolicy.oldestActive(List.of())).isEmpty();
        }

        @Test
        void consolidate_WithTimestampTie_UsesSmallerIdentifier() {
            var old = match(1, MatchStatus.AWAITING_ACCEPTANCE, Instant.EPOCH, 2);
            var newer = match(2, MatchStatus.IN_TRANSIT, Instant.EPOCH, 2);
            assertThat(DonationStatusPolicy.consolidate(10, List.of(newer, old)))
                    .isEqualTo(DonationStatus.AWAITING_ACCEPTANCE);
            assertThat(DonationStatusPolicy.activeCount(List.of(old, newer))).isEqualTo(2);
        }

        @Test
        void activeCount_WithMixedProposalStates_ExcludesEveryTerminalState() {
            var proposals =
                    List.of(
                            match(1, MatchStatus.AWAITING_ACCEPTANCE, Instant.EPOCH, 2),
                            match(2, MatchStatus.IN_TRANSIT, Instant.EPOCH, 3),
                            match(3, MatchStatus.COMPLETED, Instant.EPOCH, 4),
                            match(4, MatchStatus.CANCELLED, Instant.EPOCH, 5),
                            match(5, MatchStatus.REJECTED, Instant.EPOCH, 6));

            assertThat(DonationStatusPolicy.activeCount(proposals)).isEqualTo(2);
            assertThat(DonationStatusPolicy.allocatedQuantity(proposals)).isEqualTo(9);
        }

        @Test
        void oldestActive_WithDifferentDates_IgnoresOlderTerminalProposalAndIdentifierOrder() {
            var terminal = match(1, MatchStatus.COMPLETED, Instant.EPOCH, 2);
            var olderActive = match(9, MatchStatus.IN_TRANSIT, Instant.EPOCH.plusSeconds(1), 2);
            var newerActive =
                    match(2, MatchStatus.AWAITING_ACCEPTANCE, Instant.EPOCH.plusSeconds(2), 2);

            assertThat(
                            DonationStatusPolicy.oldestActive(
                                    List.of(newerActive, terminal, olderActive)))
                    .contains(olderActive);
            assertThat(
                            DonationStatusPolicy.consolidate(
                                    10, List.of(newerActive, terminal, olderActive)))
                    .isEqualTo(DonationStatus.IN_TRANSIT);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 5, 50, 100, Integer.MAX_VALUE})
    void pagination_WithValidCurrentPage_KeepsBoundsAndEnds(int current) {
        var page = new PageResponse<>(List.of(), current, 6, 600, 100);
        assertThat(page.visiblePages()).contains(1, 100).isSorted();
        assertThat(page.hasPrevious()).isEqualTo(current > 1);
        assertThat(page.hasNext()).isEqualTo(current < 100);
    }

    @Test
    void pagination_WithEmptyResult_HasNoLinks() {
        assertThat(new PageResponse<>(List.of(), 1, 6, 0, 0).visiblePages()).isEmpty();
    }

    @Test
    void search_WithSqlWildcards_EscapesLiteralCharacters() {
        assertThat(SearchPatterns.contains("  A%_\\B  ")).isEqualTo("%a\\%\\_\\\\b%");
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://example.test", "http://127.0.0.1:8080/root/"})
    void recoveryUrl_WithSafeBase_AppendsToken(String base) {
        var p =
                new PasswordRecoveryProperties(
                        true, "fake@example.test", base, Duration.ofMinutes(30));
        assertThat(p.resetUrl("fictitious")).endsWith("/password/reset?token=fictitious");
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "file:///tmp",
                "https://user:pass@example.test",
                "https://example.test?x=1",
                "https://example.test#fragment",
                "relative"
            })
    void recoveryUrl_WithUnsafeBase_ThrowsException(String base) {
        assertThatIllegalArgumentException()
                .isThrownBy(
                        () ->
                                new PasswordRecoveryProperties(
                                        true, "fake@example.test", base, Duration.ofMinutes(30)));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 59, 86401})
    void recoveryUrl_WithInvalidTtl_ThrowsException(long seconds) {
        assertThatIllegalArgumentException()
                .isThrownBy(
                        () ->
                                new PasswordRecoveryProperties(
                                        true,
                                        "fake@example.test",
                                        "http://localhost",
                                        Duration.ofSeconds(seconds)));
    }
}
