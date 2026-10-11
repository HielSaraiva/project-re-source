package edu.br.resource.resourcesystem.dto.response;

import edu.br.resource.resourcesystem.model.enums.MatchEventType;
import java.time.Instant;

public record DonationHistoryEventResponse(
        Integer id,
        MatchEventType type,
        String title,
        Instant at,
        String actor,
        String description) {}
