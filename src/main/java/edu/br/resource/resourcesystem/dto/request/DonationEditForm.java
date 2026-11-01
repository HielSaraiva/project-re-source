package edu.br.resource.resourcesystem.dto.request;

import edu.br.resource.resourcesystem.model.enums.ItemCondition;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DonationEditForm {
    @NotBlank(message = "Informe o nome do item.")
    @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
    private String title;

    @NotNull(message = "Informe a quantidade.")
    @Positive(message = "A quantidade deve ser maior que zero.")
    @tools.jackson.databind.annotation.JsonDeserialize(
            using = edu.br.resource.resourcesystem.validation.WholeQuantityDeserializer.class)
    private Integer quantity;

    @NotNull(message = "Selecione a condição do item.")
    private ItemCondition condition;

    @NotNull(message = "Recarregue os dados antes de salvar.")
    @jakarta.validation.constraints.PositiveOrZero
    private Long version;

    @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres.")
    private String description;
}
