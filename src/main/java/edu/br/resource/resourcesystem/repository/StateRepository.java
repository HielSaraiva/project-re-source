package edu.br.resource.resourcesystem.repository;

import edu.br.resource.resourcesystem.model.entity.State;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StateRepository extends JpaRepository<State, Integer> {
    List<State> findAllByOrderByNameAsc();
}
