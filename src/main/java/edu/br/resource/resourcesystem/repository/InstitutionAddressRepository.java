package edu.br.resource.resourcesystem.repository;

import edu.br.resource.resourcesystem.model.entity.InstitutionAddress;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstitutionAddressRepository extends JpaRepository<InstitutionAddress, Integer> {
    @EntityGraph(attributePaths = {"city", "city.state"})
    Optional<InstitutionAddress> findByInstitutionId(Integer institutionId);
}
