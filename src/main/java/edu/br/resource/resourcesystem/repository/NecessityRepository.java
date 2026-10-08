package edu.br.resource.resourcesystem.repository;

import edu.br.resource.resourcesystem.model.entity.Necessity;
import edu.br.resource.resourcesystem.model.enums.NecessityStatus;
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
public interface NecessityRepository extends JpaRepository<Necessity, Integer>, JpaSpecificationExecutor<Necessity> {
    @Override
    @EntityGraph(attributePaths = {"institution", "itemType"})
    Page<Necessity> findAll(Specification<Necessity> specification, Pageable pageable);

    @EntityGraph(attributePaths = {"institution", "itemType"})
    Optional<Necessity> findByIdAndStatus(Integer id, NecessityStatus status);

    long countByInstitutionIdAndStatus(Integer institutionId, NecessityStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from Necessity n where n.id = :id")
    Optional<Necessity> findForUpdate(@Param("id") Integer id);
}
