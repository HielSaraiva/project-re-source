package edu.br.resource.resourcesystem;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.br.resource.resourcesystem.dto.request.NecessityFilterRequest;
import edu.br.resource.resourcesystem.dto.response.PageResponse;
import edu.br.resource.resourcesystem.model.entity.Institution;
import edu.br.resource.resourcesystem.model.entity.ItemType;
import edu.br.resource.resourcesystem.model.entity.Necessity;
import edu.br.resource.resourcesystem.model.enums.*;
import edu.br.resource.resourcesystem.repository.InstitutionRepository;
import edu.br.resource.resourcesystem.repository.ItemTypeRepository;
import edu.br.resource.resourcesystem.repository.NecessityRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class ResourceSystemApplicationTests {
    @Autowired private MockMvc mvc;
    @Autowired private InstitutionRepository institutions;
    @Autowired private ItemTypeRepository itemTypes;
    @Autowired private NecessityRepository necessities;

    @Test
    void contextLoads() {}

    @Test
    @Transactional
    @WithMockUser(username = "ong@example.org", roles = "ONG")
    void institutionCanViewItsNeedsAndPriorities() throws Exception {
        var institution =
                institutions.save(
                        Institution.builder()
                                .cnpj("12345678000199")
                                .legalName("Instituto ReSource")
                                .email("ong@example.org")
                                .passwordHash("not-used-in-this-test")
                                .representativeFullName("Representante")
                                .representativeCpf("12345678901")
                                .status(InstitutionStatus.APPROVED)
                                .build());
        var itemType =
                itemTypes.save(
                        ItemType.builder()
                                .category(ItemCategory.FURNITURE)
                                .name("Cadeiras escolares")
                                .build());
        necessities.save(
                Necessity.builder()
                        .institution(institution)
                        .itemType(itemType)
                        .quantityRequested(50)
                        .priority(NecessityPriority.HIGH)
                        .status(NecessityStatus.ACTIVE)
                        .build());
        var fulfilledItemType =
                itemTypes.save(
                        ItemType.builder()
                                .category(ItemCategory.BEDDING)
                                .name("Cobertores atendidos")
                                .build());
        necessities.save(
                Necessity.builder()
                        .institution(institution)
                        .itemType(fulfilledItemType)
                        .quantityRequested(10)
                        .priority(NecessityPriority.LOW)
                        .status(NecessityStatus.FULFILLED)
                        .build());

        var result =
                mvc.perform(get("/institution/necessities"))
                        .andExpect(status().isOk())
                        .andExpect(view().name("institution/necessities/list"))
                        .andExpect(content().string(containsString("Cadeiras escolares")))
                        .andExpect(content().string(containsString("Alta prioridade")))
                        .andExpect(content().string(containsString("Quantidade solicitada")))
                        .andExpect(content().string(containsString("Quantidade reservada")))
                        .andExpect(content().string(containsString("Quantidade recebida")))
                        .andExpect(
                                content().string(containsString("Disponível para novas propostas")))
                        .andExpect(content().string(containsString("Excluir necessidade")))
                        .andExpect(content().string(containsString("Concluídas")))
                        .andExpect(content().string(containsString("Excluir necessidade?")))
                        .andExpect(content().string(containsString("Limpar filtros")))
                        .andExpect(
                                content()
                                        .string(
                                                org.hamcrest.Matchers.not(
                                                        containsString("Aplicar filtros"))))
                        .andExpect(content().string(containsString("value=\"active\"")))
                        .andExpect(content().string(containsString("necessity-quantity-hint")))
                        .andReturn();

        var defaultFilter =
                (NecessityFilterRequest) result.getModelAndView().getModel().get("filter");
        org.junit.jupiter.api.Assertions.assertEquals(
                NecessityStatus.ACTIVE, defaultFilter.status());
        org.junit.jupiter.api.Assertions.assertFalse(
                result.getResponse().getContentAsString().contains("Cobertores atendidos"));
        org.junit.jupiter.api.Assertions.assertFalse(
                result.getResponse().getContentAsString().contains("Número da necessidade"));
        org.junit.jupiter.api.Assertions.assertTrue(
                result.getResponse().getContentAsString().contains("data-delete-id"));
        org.junit.jupiter.api.Assertions.assertFalse(
                result.getResponse().getContentAsString().contains("Exclusão bloqueada"));

        mvc.perform(get("/institution/necessities?priority=low"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("necessities", hasSize(0)));
        mvc.perform(get("/institution/necessities?status=fulfilled"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cobertores atendidos")))
                .andExpect(content().string(containsString("Concluída")))
                .andExpect(content().string(containsString("Quantidade recebida")))
                .andExpect(
                        content()
                                .string(
                                        org.hamcrest.Matchers.not(
                                                containsString("Quantidade reservada"))))
                .andExpect(
                        content()
                                .string(
                                        org.hamcrest.Matchers.not(
                                                containsString("Disponível para novas propostas"))))
                .andExpect(
                        content()
                                .string(
                                        org.hamcrest.Matchers.not(
                                                containsString(
                                                        "Não é possível excluir esta necessidade"
                                                                + " porque existem propostas"))))
                .andExpect(
                        content()
                                .string(
                                        org.hamcrest.Matchers.not(
                                                containsString(
                                                        "Edição indisponível para necessidades"
                                                                + " atendida"))));
        mvc.perform(get("/institution/necessities?query=nao-encontrada"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nenhuma necessidade encontrada")));

        for (int index = 0; index < 9; index++) {
            var pagedItem =
                    itemTypes.save(
                            ItemType.builder()
                                    .category(ItemCategory.OTHER)
                                    .name("Item de teste " + index)
                                    .build());
            necessities.save(
                    Necessity.builder()
                            .institution(institution)
                            .itemType(pagedItem)
                            .description("Item de teste " + index)
                            .quantityRequested(2)
                            .priority(NecessityPriority.MEDIUM)
                            .status(NecessityStatus.ACTIVE)
                            .build());
        }
        var pageTwo =
                mvc.perform(get("/institution/necessities?page=2"))
                        .andExpect(status().isOk())
                        .andExpect(model().attribute("necessities", hasSize(1)))
                        .andReturn();
        var pagination = (PageResponse<?>) pageTwo.getModelAndView().getModel().get("pagination");
        org.junit.jupiter.api.Assertions.assertEquals(2, pagination.page());
    }

    @Test
    void unauthenticatedNeedPageRedirectsToInstitutionLoginAndIsSaved() throws Exception {
        mvc.perform(get("/institution/necessities"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?profile=institution"))
                .andExpect(
                        request()
                                .sessionAttribute(
                                        "SPRING_SECURITY_SAVED_REQUEST",
                                        org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    @WithMockUser(username = "ong@example.org", roles = "ONG")
    void necessityApiRejectsFractionalQuantities() throws Exception {
        mvc.perform(
                        post("/api/institution/necessities")
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "category": "furniture",
                                          "description": "Cadeiras",
                                          "quantity": 1.9,
                                          "priority": "high"
                                        }
                                        """))
                .andExpect(status().isBadRequest());
    }
}
