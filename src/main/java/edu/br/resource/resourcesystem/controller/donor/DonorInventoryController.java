package edu.br.resource.resourcesystem.controller.donor;

import edu.br.resource.resourcesystem.dto.request.*;
import edu.br.resource.resourcesystem.dto.response.*;
import edu.br.resource.resourcesystem.security.CurrentActor;
import edu.br.resource.resourcesystem.service.*;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/donor/inventory")
public class DonorInventoryController {
    private final DonationManagementService management;
    private final DonationHistoryService history;
    private final CurrentActor actors;

    public record Created(Integer id) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Created create(Authentication auth, @Valid @RequestBody NewDonationForm form) {
        return new Created(management.register(actors.donor(auth).getId(), form));
    }

    @GetMapping("/{id}")
    public DonationHistoryResponse details(Authentication auth, @PathVariable Integer id) {
        return history.details(actors.donor(auth).getId(), id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            Authentication auth,
            @PathVariable Integer id,
            @Valid @RequestBody DonationEditForm form) {
        management.update(actors.donor(auth).getId(), id, form);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication auth, @PathVariable Integer id, @RequestParam long version) {
        management.delete(actors.donor(auth).getId(), id, version);
    }

    @GetMapping("/{id}/proposals")
    public PageResponse<DonationProposalResponse> proposals(
            Authentication auth,
            @PathVariable Integer id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false)
                    edu.br.resource.resourcesystem.model.enums.MatchStatus status) {
        return history.proposals(actors.donor(auth).getId(), id, page, status);
    }
}
