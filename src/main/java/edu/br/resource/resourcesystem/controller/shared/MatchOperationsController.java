package edu.br.resource.resourcesystem.controller.shared;

import edu.br.resource.resourcesystem.dto.request.*;
import edu.br.resource.resourcesystem.dto.response.*;
import edu.br.resource.resourcesystem.security.CurrentActor;
import edu.br.resource.resourcesystem.service.match.MatchWorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MatchOperationsController {
    private final MatchWorkflowService workflow;
    private final CurrentActor actors;

    @PostMapping("/donor/api/donations")
    public InventoryDonationResponse register(Authentication auth,
            @Valid @RequestBody RegisterDonationRequest request) {
        return workflow.register(actors.donor(auth).getId(), request);
    }

    @PostMapping("/donor/api/proposals")
    public MatchDetailResponse propose(Authentication auth, @Valid @RequestBody SubmitProposalRequest request) {
        return workflow.propose(actors.donor(auth).getId(), request);
    }

    @PostMapping("/donor/api/matches/{protocol}/cancel")
    public MatchDetailResponse cancel(Authentication auth, @PathVariable String protocol,
            @Valid @RequestBody CancelDonationRequest request) {
        return workflow.cancel(actors.donor(auth).getId(), protocol);
    }

    @PostMapping("/donor/api/matches/{protocol}/method")
    public MatchDetailResponse method(Authentication auth, @PathVariable String protocol,
            @Valid @RequestBody SelectDeliveryMethodRequest request) {
        return workflow.selectMethod(actors.donor(auth).getId(), protocol, request.method());
    }

    @PostMapping("/donor/api/matches/{protocol}/in-person")
    public MatchDetailResponse deliver(Authentication auth, @PathVariable String protocol,
            @Valid @RequestBody ConfirmInPersonDeliveryRequest request) {
        return workflow.reportDelivery(actors.donor(auth).getId(), protocol, request);
    }

    @PostMapping("/donor/api/matches/{protocol}/shipment")
    public MatchDetailResponse ship(Authentication auth, @PathVariable String protocol,
            @Valid @RequestBody ConfirmCarrierShipmentRequest request) {
        return workflow.reportShipment(actors.donor(auth).getId(), protocol, request);
    }

    @PostMapping("/ong/api/matches/{protocol}/accept")
    public MatchDetailResponse accept(Authentication auth, @PathVariable String protocol,
            @Valid @RequestBody AcceptProposalRequest request) {
        return workflow.accept(actors.institution(auth).getId(), protocol);
    }

    @PostMapping("/ong/api/matches/{protocol}/reject")
    public MatchDetailResponse reject(Authentication auth, @PathVariable String protocol,
            @Valid @RequestBody RejectProposalRequest request) {
        return workflow.reject(actors.institution(auth).getId(), protocol, request.reason());
    }

    @PostMapping("/ong/api/matches/{protocol}/receipt")
    public MatchDetailResponse receive(Authentication auth, @PathVariable String protocol,
            @Valid @RequestBody ConfirmReceiptRequest request) {
        return workflow.receive(actors.institution(auth).getId(), protocol);
    }
}
