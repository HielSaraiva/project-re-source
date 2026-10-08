package edu.br.resource.resourcesystem.dto.response;

import edu.br.resource.resourcesystem.model.enums.DonationStage;
import java.time.Instant;

public record DonationDeadlineResponse(
        Instant acceptance,
        Instant methodSelection,
        Instant inPersonDelivery,
        Instant carrierShipment,
        DonationStage activeStage,
        Instant activeDeadline) {
}
