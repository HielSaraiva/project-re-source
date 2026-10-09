package edu.br.resource.resourcesystem.messaging;

import edu.br.resource.resourcesystem.config.MessagingConfiguration;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatchOutboxPublisher {
    private final MatchOutboxStore store;
    private final RabbitTemplate rabbit;
    @Value("${resource.messaging.batch-size:50}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${resource.messaging.publish-delay-ms:1000}", initialDelay = 5000)
    public void publishPending() {
        UUID token = UUID.randomUUID();
        var ids = store.claim(token, Math.max(1, Math.min(batchSize, 200)), 60);
        var confirmed = new java.util.ArrayList<UUID>();
        var confirmations = new LinkedHashMap<UUID, CorrelationData>();
        try {
            for (UUID id : ids) {
                var properties = new MessageProperties();
                properties.setMessageId(id.toString());
                properties.setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN);
                properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                var correlation = new CorrelationData(id.toString());
                rabbit.send(MessagingConfiguration.EXCHANGE, MessagingConfiguration.ROUTING_KEY,
                        new Message(id.toString().getBytes(StandardCharsets.UTF_8), properties), correlation);
                confirmations.put(id, correlation);
            }
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            for (var entry : confirmations.entrySet()) {
                var correlation = entry.getValue();
                var confirm = correlation.getFuture().get(Math.max(1, deadline - System.nanoTime()),
                        TimeUnit.NANOSECONDS);
                if (!confirm.ack() || correlation.getReturned() != null)
                    throw new IllegalStateException("Publicação não confirmada ou sem rota.");
                confirmed.add(entry.getKey());
            }
        } catch (Exception failure) {
            if (failure instanceof InterruptedException)
                Thread.currentThread().interrupt();
            log.warn("Lote de {} eventos aguardará nova tentativa: {}", ids.size(), failure.getClass().getSimpleName());
        } finally {
            store.finish(ids, confirmed, token);
        }
    }
}
