package edu.br.resource.resourcesystem.repository;

import edu.br.resource.resourcesystem.model.entity.ItemType;
import edu.br.resource.resourcesystem.model.enums.ItemCategory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemTypeRepository extends JpaRepository<ItemType, Integer> {
    List<ItemType> findAllByCategoryOrderByNameAsc(ItemCategory category);

    @org.springframework.data.jpa.repository.Query(
            value =
                    """
                    INSERT INTO item_types(category, name) VALUES (CAST(:category AS item_category), :name)
                    ON CONFLICT (category, lower(btrim(name))) DO UPDATE SET name = item_types.name
                    RETURNING id
                    """,
            nativeQuery = true)
    Integer obtainId(
            @org.springframework.data.repository.query.Param("category") String category,
            @org.springframework.data.repository.query.Param("name") String name);

    Optional<ItemType> findByCategoryAndNameIgnoreCase(ItemCategory category, String name);
}
