package edu.br.resource.resourcesystem.messaging;

import edu.br.resource.resourcesystem.config.MessagingConfiguration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import edu.br.resource.resourcesystem.repository.MatchEventRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class MatchEventConsumer {
    private final MatchEventRepository events;

    @RabbitListener(queues = MessagingConfiguration.QUEUE)
    @Transactional
    public void recordNotification(String eventId) {
        try {
            events.recordNotification(UUID.fromString(eventId));
        } catch (IllegalArgumentException failure) {
            log.warn("event=notification_rejected reason=invalid_or_unknown_event");
            throw new AmqpRejectAndDontRequeueException("Evento inválido ou desconhecido.", failure);
        }
    }
}
