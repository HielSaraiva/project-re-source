package edu.br.resource.resourcesystem.repository;

import edu.br.resource.resourcesystem.model.entity.State;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StateRepository extends JpaRepository<State, Integer> {
    List<State> findAllByOrderByNameAsc();
}
