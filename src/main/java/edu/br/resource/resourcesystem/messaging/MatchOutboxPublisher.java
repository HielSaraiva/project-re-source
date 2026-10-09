package edu.br.resource.resourcesystem.messaging;

import edu.br.resource.resourcesystem.config.MessagingConfiguration;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import edu.br.resource.resourcesystem.repository.MatchEventRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatchOutboxPublisher {
    private final MatchEventRepository events;
    private final RabbitTemplate rabbit;

    @Scheduled(fixedDelayString = "${resource.messaging.publish-delay-ms:2000}",initialDelay = 5000)
    @Transactional
    public void publishPending() {
        for (UUID id : events.pendingForUpdate()) {
            try {
                var properties=new MessageProperties();properties.setMessageId(id.toString());properties.setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN);properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                var correlation=new CorrelationData(id.toString());
                rabbit.send(MessagingConfiguration.EXCHANGE,MessagingConfiguration.ROUTING_KEY,new Message(id.toString().getBytes(StandardCharsets.UTF_8),properties),correlation);
                var confirm=correlation.getFuture().get(5,TimeUnit.SECONDS);
                if(!confirm.ack() || correlation.getReturned()!=null)throw new IllegalStateException("Evento não confirmado pelo broker.");
                events.markPublished(id);
            } catch(Exception failure){
                if(failure instanceof InterruptedException)Thread.currentThread().interrupt();
                events.scheduleRetry(id);
                log.warn("Evento {} permanece na outbox para nova tentativa: {}",id,failure.getClass().getSimpleName());
                break;
            }
        }
    }
}
