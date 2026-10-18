package edu.br.resource.resourcesystem;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

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

        mvc.perform(get("/institution/necessities"))
                .andExpect(status().isOk())
                .andExpect(view().name("institution/necessities/list"))
                .andExpect(content().string(containsString("Cadeiras escolares")))
                .andExpect(content().string(containsString("Alta prioridade")));
    }
}
