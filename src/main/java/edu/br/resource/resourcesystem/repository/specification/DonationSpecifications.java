package edu.br.resource.resourcesystem.repository.specification;

import edu.br.resource.resourcesystem.model.entity.Donation;
import edu.br.resource.resourcesystem.model.entity.DonationMatch;
import edu.br.resource.resourcesystem.model.enums.ItemCategory;
import edu.br.resource.resourcesystem.model.enums.MatchStatus;
import org.springframework.data.jpa.domain.Specification;

public final class DonationSpecifications {
    private DonationSpecifications() {}

    public static Specification<Donation> forDonor(Integer donorId) {
        return (root, query, builder) -> builder.equal(root.get("donor").get("id"), donorId);
    }

    public static Specification<Donation> withCategory(ItemCategory category) {
        return (root, query, builder) -> category == null ? builder.conjunction()
                : builder.equal(root.get("donationPackage").get("item").get("itemType").get("category"), category);
    }

    public static Specification<Donation> withAvailableQuantity() {
        return (root, query, builder) -> {
            var allocation = query.subquery(Long.class);
            var match = allocation.from(DonationMatch.class);
            allocation.select(builder.coalesce(builder.sumAsLong(match.get("allocatedQuantity")), 0L))
                    .where(builder.equal(match.get("donation"), root), match.get("status").in(MatchStatus.allocationConsumingStatuses()));
            return builder.lessThan(allocation, root.get("donationPackage").get("quantity").as(Long.class));
        };
    }
}
