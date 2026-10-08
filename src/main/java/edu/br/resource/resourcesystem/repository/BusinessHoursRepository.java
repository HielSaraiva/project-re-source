package edu.br.resource.resourcesystem.repository;

import edu.br.resource.resourcesystem.model.entity.BusinessHours;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessHoursRepository extends JpaRepository<BusinessHours, Integer> {
    @EntityGraph(attributePaths = "openDays")
    List<BusinessHours> findAllByInstitutionIdOrderByOpeningHourAsc(Integer institutionId);
}
