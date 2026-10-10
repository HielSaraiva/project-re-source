package edu.br.resource.resourcesystem.repository.specification;

import edu.br.resource.resourcesystem.model.entity.DonationMatch;
import edu.br.resource.resourcesystem.model.entity.Necessity;
import edu.br.resource.resourcesystem.model.enums.ItemCategory;
import edu.br.resource.resourcesystem.model.enums.MatchStatus;
import edu.br.resource.resourcesystem.model.enums.NecessityPriority;
import edu.br.resource.resourcesystem.model.enums.NecessityStatus;
import org.springframework.data.jpa.domain.Specification;

public final class NecessitySpecifications {
    private NecessitySpecifications() {}

    public static Specification<Necessity> forInstitution(Integer institutionId) {
        return (root, query, builder) ->
                builder.equal(root.get("institution").get("id"), institutionId);
    }

    public static Specification<Necessity> withCategory(ItemCategory category) {
        return (root, query, builder) ->
                category == null
                        ? builder.conjunction()
                        : builder.equal(root.get("itemType").get("category"), category);
    }

    public static Specification<Necessity> withPriority(NecessityPriority priority) {
        return (root, query, builder) ->
                priority == null
                        ? builder.conjunction()
                        : builder.equal(root.get("priority"), priority);
    }

    public static Specification<Necessity> inState(Integer stateId) {
        return (root, query, builder) -> {
            if (stateId == null) return builder.conjunction();
            var addressQuery = query.subquery(Integer.class);
            var address =
                    addressQuery.from(
                            edu.br.resource.resourcesystem.model.entity.InstitutionAddress.class);
            addressQuery
                    .select(address.get("institution").get("id"))
                    .where(builder.equal(address.get("city").get("state").get("id"), stateId));
            return root.get("institution").get("id").in(addressQuery);
        };
    }

    public static Specification<Necessity> search(String term) {
        return (root, query, builder) -> {
            if (term == null || term.isBlank()) return builder.conjunction();
            String pattern = SearchPatterns.contains(term);
            return builder.or(
                    builder.like(builder.lower(root.get("itemType").get("name")), pattern, '\\'),
                    builder.like(
                            builder.lower(root.get("institution").get("legalName")), pattern, '\\'),
                    builder.like(builder.lower(root.get("description")), pattern, '\\'));
        };
    }

    public static Specification<Necessity> withRemainingQuantity() {
        return (root, query, builder) -> {
            var allocation = query.subquery(Long.class);
            var match = allocation.from(DonationMatch.class);
            allocation
                    .select(builder.coalesce(builder.sumAsLong(match.get("allocatedQuantity")), 0L))
                    .where(
                            builder.equal(match.get("necessity"), root),
                            match.get("status").in(MatchStatus.allocationConsumingStatuses()));
            return builder.and(
                    builder.equal(root.get("status"), NecessityStatus.ACTIVE),
                    builder.equal(
                            root.get("institution").get("status"),
                            edu.br.resource.resourcesystem.model.enums.InstitutionStatus.APPROVED),
                    builder.lessThan(allocation, root.get("quantityRequested").as(Long.class)));
        };
    }
}
