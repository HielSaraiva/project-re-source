package edu.br.resource.resourcesystem.repository;

import edu.br.resource.resourcesystem.model.entity.BusinessHours;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusinessHoursRepository extends JpaRepository<BusinessHours, Integer> {
    @EntityGraph(attributePaths = "openDays")
    List<BusinessHours> findAllByInstitutionIdOrderByOpeningHourAsc(Integer institutionId);
}
