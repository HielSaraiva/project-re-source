package edu.br.resource.resourcesystem.repository;

import edu.br.resource.resourcesystem.model.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {}
