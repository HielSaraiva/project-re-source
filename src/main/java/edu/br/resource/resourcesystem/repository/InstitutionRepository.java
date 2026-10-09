package edu.br.resource.resourcesystem.repository;

import edu.br.resource.resourcesystem.model.entity.Institution;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, Integer> {
    Optional<Institution> findByEmailIgnoreCase(String email);
}
