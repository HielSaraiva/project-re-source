package edu.br.resource.resourcesystem.repository;

import edu.br.resource.resourcesystem.model.entity.ItemType;
import edu.br.resource.resourcesystem.model.enums.ItemCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemTypeRepository extends JpaRepository<ItemType, Integer> {
    List<ItemType> findAllByCategoryOrderByNameAsc(ItemCategory category);

    Optional<ItemType> findByCategoryAndNameIgnoreCase(ItemCategory category, String name);
}
