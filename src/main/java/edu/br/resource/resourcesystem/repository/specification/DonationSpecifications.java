package edu.br.resource.resourcesystem.repository.specification;

import edu.br.resource.resourcesystem.model.entity.Donation;
import edu.br.resource.resourcesystem.model.entity.DonationMatch;
import edu.br.resource.resourcesystem.model.enums.ItemCategory;
import edu.br.resource.resourcesystem.model.enums.MatchStatus;

import org.springframework.data.jpa.domain.Specification;

public final class DonationSpecifications {
    private DonationSpecifications() {}

    public static Specification<Donation> forDonor(Integer donorId) {
        return (root, query, builder) ->
                builder.and(
                        builder.equal(root.get("donor").get("id"), donorId),
                        builder.isNull(root.get("deletedAt")));
    }

    public static Specification<Donation> inventory(Integer donorId, boolean archived) {
        return (root, query, builder) ->
                builder.and(
                        builder.equal(root.get("donor").get("id"), donorId),
                        archived
                                ? builder.isNotNull(root.get("deletedAt"))
                                : builder.isNull(root.get("deletedAt")));
    }

    public static Specification<Donation> withCategory(ItemCategory category) {
        return (root, query, builder) ->
                category == null
                        ? builder.conjunction()
                        : builder.equal(
                                root.get("donationPackage")
                                        .get("item")
                                        .get("itemType")
                                        .get("category"),
                                category);
    }

    public static Specification<Donation> withStatus(
            edu.br.resource.resourcesystem.model.enums.DonationStatus status) {
        return (root, query, builder) ->
                status == null ? builder.conjunction() : builder.equal(root.get("status"), status);
    }

    public static Specification<Donation> search(String term) {
        return (root, query, builder) -> {
            if (term == null || term.isBlank()) return builder.conjunction();
            String pattern = SearchPatterns.contains(term);
            var proposal = query.subquery(Integer.class);
            var match = proposal.from(DonationMatch.class);
            proposal.select(match.get("id"))
                    .where(
                            builder.equal(match.get("donation"), root),
                            builder.or(
                                    builder.like(
                                            builder.lower(match.get("protocol")), pattern, '\\'),
                                    builder.like(
                                            builder.lower(
                                                    match.get("necessity")
                                                            .get("institution")
                                                            .get("legalName")),
                                            pattern,
                                            '\\')));
            var text =
                    builder.or(
                            builder.like(
                                    builder.lower(
                                            root.get("donationPackage").get("item").get("title")),
                                    pattern,
                                    '\\'),
                            builder.like(
                                    builder.lower(
                                            root.get("donationPackage")
                                                    .get("item")
                                                    .get("description")),
                                    pattern,
                                    '\\'),
                            builder.exists(proposal));
            try {
                int id = Integer.parseInt(term.strip().replaceFirst("^#", ""));
                return builder.or(text, builder.equal(root.get("id"), id));
            } catch (NumberFormatException ignored) {
                return text;
            }
        };
    }

    public static Specification<Donation> withAvailableQuantity() {
        return (root, query, builder) -> {
            var allocation = query.subquery(Long.class);
            var match = allocation.from(DonationMatch.class);
            allocation
                    .select(builder.coalesce(builder.sumAsLong(match.get("allocatedQuantity")), 0L))
                    .where(
                            builder.equal(match.get("donation"), root),
                            match.get("status").in(MatchStatus.allocationConsumingStatuses()));
            return builder.and(
                    builder.isNull(root.get("deletedAt")),
                    builder.lessThan(
                            allocation,
                            root.get("donationPackage").get("quantity").as(Long.class)));
        };
    }
}
