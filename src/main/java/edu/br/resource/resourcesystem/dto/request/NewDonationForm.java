package edu.br.resource.resourcesystem.dto.request;

import edu.br.resource.resourcesystem.model.enums.ItemCategory;
import edu.br.resource.resourcesystem.model.enums.ItemCondition;

import jakarta.validation.constraints.*;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class NewDonationForm {
    @NotNull(message = "Selecione a categoria.")
    private ItemCategory category;

    @NotBlank(message = "Informe o nome do item.")
    @Size(max = 120, message = "Use até 120 caracteres.")
    private String title;

    @NotNull(message = "Informe a quantidade.")
    @Positive(message = "Informe uma quantidade inteira maior que zero.")
    @tools.jackson.databind.annotation.JsonDeserialize(
            using = edu.br.resource.resourcesystem.validation.WholeQuantityDeserializer.class)
    private Integer quantity = 1;

    @NotNull(message = "Selecione o estado de conservação.")
    private ItemCondition condition;

    @Size(max = 1000, message = "Use até 1.000 caracteres na descrição.")
    private String description;

    @NotNull(message = "Atualize a página para iniciar um cadastro seguro.")
    private UUID submissionKey;
}
