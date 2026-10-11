package edu.br.resource.resourcesystem.repository;

import edu.br.resource.resourcesystem.model.entity.OpenDays;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OpenDaysRepository extends JpaRepository<OpenDays, Integer> {}
