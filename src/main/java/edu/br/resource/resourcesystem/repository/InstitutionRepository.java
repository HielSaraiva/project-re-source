package edu.br.resource.resourcesystem.repository;

import edu.br.resource.resourcesystem.model.entity.Institution;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, Integer> {
    Optional<Institution> findByEmailIgnoreCase(String email);

    Optional<Institution> findByCnpj(String cnpj);

    boolean existsByRepresentativeCpf(String representativeCpf);
}
