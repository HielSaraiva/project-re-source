package edu.br.resource.resourcesystem.repository.specification;

import edu.br.resource.resourcesystem.model.entity.DonationMatch;
import edu.br.resource.resourcesystem.model.enums.MatchStatus;
import org.springframework.data.jpa.domain.Specification;

public final class MatchSpecifications {
    private MatchSpecifications() {}

    public static Specification<DonationMatch> forDonor(Integer donorId) {
        return (root, query, builder) -> builder.equal(root.get("donation").get("donor").get("id"), donorId);
    }

    public static Specification<DonationMatch> forInstitution(Integer institutionId) {
        return (root, query, builder) -> builder.equal(root.get("necessity").get("institution").get("id"), institutionId);
    }

    public static Specification<DonationMatch> withStatus(MatchStatus status) {
        return (root, query, builder) -> status == null ? builder.conjunction() : builder.equal(root.get("status"), status);
    }

    public static Specification<DonationMatch> search(String term) {
        return (root, query, builder) -> {
            if (term == null || term.isBlank()) return builder.conjunction();
            String pattern = SearchPatterns.contains(term);
            return builder.or(
                    builder.like(builder.lower(root.get("protocol")), pattern, '\\'),
                    builder.like(builder.lower(root.get("donation").get("donor").get("fullName")), pattern, '\\'),
                    builder.like(builder.lower(root.get("donation").get("donationPackage").get("item").get("title")), pattern, '\\'),
                    builder.like(builder.lower(root.get("necessity").get("itemType").get("name")), pattern, '\\'),
                    builder.like(builder.lower(root.get("necessity").get("institution").get("legalName")), pattern, '\\'));
        };
    }
}
