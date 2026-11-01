package edu.br.resource.resourcesystem.controller.ong;

import edu.br.resource.resourcesystem.dto.request.ManageNecessityRequest;
import edu.br.resource.resourcesystem.dto.response.InstitutionNecessityResponse;
import edu.br.resource.resourcesystem.security.CurrentActor;
import edu.br.resource.resourcesystem.service.necessity.NecessityManagementService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/institution/necessities")
public class OngNecessityOperationsController {
    private final NecessityManagementService necessities;
    private final CurrentActor actors;

    public OngNecessityOperationsController(
            NecessityManagementService necessities, CurrentActor actors) {
        this.necessities = necessities;
        this.actors = actors;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void create(
            Authentication authentication, @Valid @RequestBody ManageNecessityRequest request) {
        necessities.create(actors.institution(authentication).getId(), request);
    }

    @GetMapping("/{id}")
    public InstitutionNecessityResponse editDetails(
            Authentication authentication, @PathVariable Integer id) {
        return necessities.editDetails(actors.institution(authentication).getId(), id);
    }

    @PutMapping("/{id}")
    public InstitutionNecessityResponse update(
            Authentication authentication,
            @PathVariable Integer id,
            @Valid @RequestBody ManageNecessityRequest request) {
        return necessities.update(actors.institution(authentication).getId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            Authentication authentication, @PathVariable Integer id, @RequestParam long version) {
        necessities.delete(actors.institution(authentication).getId(), id, version);
    }
}
