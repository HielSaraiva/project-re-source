package edu.br.resource.resourcesystem.messaging;

import edu.br.resource.resourcesystem.config.MessagingConfiguration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
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
        try {
            events.recordNotification(UUID.fromString(eventId));
        } catch (IllegalArgumentException failure) {
            throw new AmqpRejectAndDontRequeueException("Evento inválido ou desconhecido.", failure);
        }
    }
}
