package edu.br.resource.resourcesystem.messaging;

import edu.br.resource.resourcesystem.model.entity.DonationMatch;
import edu.br.resource.resourcesystem.model.enums.MatchEventType;
import lombok.RequiredArgsConstructor;
import edu.br.resource.resourcesystem.repository.MatchEventRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MatchEventOutbox {
    private final MatchEventRepository events;

    @Transactional(propagation = Propagation.MANDATORY)
    public void append(DonationMatch match, MatchEventType type) {
        events.append(match, type);
    }
}
