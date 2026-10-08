package edu.br.resource.resourcesystem.presentation;

import edu.br.resource.resourcesystem.model.enums.ItemCategory;
import org.springframework.stereotype.Component;

/** Shared category artwork for DTO mappers and Thymeleaf views. */
@Component("categoryIconResolver")
public class CategoryIconResolver {
    private static final String ASSET_DIRECTORY = "/images/shared/categories/";

    public String resolve(ItemCategory category) {
        ItemCategory effectiveCategory = category == null ? ItemCategory.OTHER : category;
        String fileName = switch (effectiveCategory) {
            case FOOD -> "food.svg";
            case CLOTHING -> "clothing.svg";
            case BEDDING -> "bedding.svg";
            case HOUSEHOLD -> "household.svg";
            case OTHER -> "package.svg";
            case TOY -> "toy.svg";
            case EDUCATION -> "education.svg";
            case HYGIENE -> "hygiene.svg";
            case MOBILITY -> "heart.svg";
            case ELECTRONICS -> "electronics.svg";
            case FURNITURE -> "furniture.svg";
        };
        return ASSET_DIRECTORY + fileName;
    }
}
