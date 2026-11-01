package edu.br.resource.resourcesystem.dto.request;

import edu.br.resource.resourcesystem.model.enums.ItemCategory;
import edu.br.resource.resourcesystem.model.enums.ItemCondition;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewDonationForm {
    @NotNull private ItemCategory category;

    @NotBlank
    @Size(max = 120)
    private String title;

    @NotNull @Positive private Integer quantity = 1;

    @NotNull private ItemCondition condition;
}
