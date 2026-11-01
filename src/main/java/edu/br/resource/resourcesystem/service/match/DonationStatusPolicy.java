package edu.br.resource.resourcesystem.service.match;

import edu.br.resource.resourcesystem.model.entity.DonationMatch;
import edu.br.resource.resourcesystem.model.enums.DonationStatus;
import edu.br.resource.resourcesystem.model.enums.MatchStatus;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

public final class DonationStatusPolicy {
    private static final Comparator<DonationMatch> OLDEST_FIRST =
            Comparator.comparing(DonationMatch::getCreatedAt).thenComparing(DonationMatch::getId);

    private DonationStatusPolicy() {}

    public static Optional<DonationMatch> oldestActive(Collection<DonationMatch> matches) {
        return matches.stream()
                .filter(match -> MatchStatus.activeStatuses().contains(match.getStatus()))
                .min(OLDEST_FIRST);
    }

    public static long activeCount(Collection<DonationMatch> matches) {
        return matches.stream()
                .filter(match -> MatchStatus.activeStatuses().contains(match.getStatus()))
                .count();
    }

    public static long allocatedQuantity(Collection<DonationMatch> matches) {
        return matches.stream()
                .filter(
                        match ->
                                MatchStatus.allocationConsumingStatuses()
                                        .contains(match.getStatus()))
                .mapToLong(DonationMatch::getAllocatedQuantity)
                .sum();
    }

    public static DonationStatus consolidate(
            Integer registeredQuantity, Collection<DonationMatch> matches) {
        return oldestActive(matches)
                .map(
                        match ->
                                match.getStatus() == MatchStatus.PROPOSED
                                        ? DonationStatus.AWAITING_ACCEPTANCE
                                        : DonationStatus.fromValue(match.getStatus().getValue()))
                .orElse(
                        allocatedQuantity(matches) >= registeredQuantity
                                ? DonationStatus.COMPLETED
                                : DonationStatus.REGISTERED);
    }
}
