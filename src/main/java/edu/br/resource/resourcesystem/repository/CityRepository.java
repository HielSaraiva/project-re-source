package edu.br.resource.resourcesystem.repository;

import edu.br.resource.resourcesystem.model.entity.City;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends JpaRepository<City, Integer> {
    @EntityGraph(attributePaths = "state")
    List<City> findAllByStateIdOrderByNameAsc(Integer stateId);
}
