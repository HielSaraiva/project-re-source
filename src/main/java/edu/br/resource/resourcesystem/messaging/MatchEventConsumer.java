package edu.br.resource.resourcesystem.messaging;

import edu.br.resource.resourcesystem.config.MessagingConfiguration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import edu.br.resource.resourcesystem.repository.MatchEventRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MatchEventConsumer {
    private final MatchEventRepository events;

    @RabbitListener(queues = MessagingConfiguration.QUEUE)
    @Transactional
    public void recordNotification(String eventId) {
        events.recordNotification(UUID.fromString(eventId));
    }
}
