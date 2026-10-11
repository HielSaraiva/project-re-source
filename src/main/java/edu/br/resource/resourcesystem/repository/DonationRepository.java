package edu.br.resource.resourcesystem.repository;

import edu.br.resource.resourcesystem.model.entity.Donation;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DonationRepository
        extends JpaRepository<Donation, Integer>, JpaSpecificationExecutor<Donation> {
    @Override
    @EntityGraph(attributePaths = {"donationPackage.item.itemType"})
    Page<Donation> findAll(Specification<Donation> specification, Pageable pageable);

    @EntityGraph(
            attributePaths = {
                "donationPackage",
                "donationPackage.item",
                "donationPackage.item.itemType"
            })
    Optional<Donation> findByIdAndDonorId(Integer id, Integer donorId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Donation d where d.id = :id and d.donor.id = :donorId")
    Optional<Donation> findForUpdate(@Param("id") Integer id, @Param("donorId") Integer donorId);
}
