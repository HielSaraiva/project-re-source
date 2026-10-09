package edu.br.resource.resourcesystem.messaging;

import edu.br.resource.resourcesystem.repository.MatchEventRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class MatchOutboxStore {
    private final MatchEventRepository events;

    public List<UUID> claim(UUID token, int batchSize, int leaseSeconds) {
        return events.claimPending(token, batchSize, leaseSeconds);
    }

    public void finish(List<UUID> ids, List<UUID> confirmed, UUID token) {
        events.markPublished(confirmed, token);
        var pending = new java.util.HashSet<>(ids);
        pending.removeAll(confirmed);
        events.scheduleRetry(List.copyOf(pending), token);
    }
}
