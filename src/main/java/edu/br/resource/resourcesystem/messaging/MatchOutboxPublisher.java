package edu.br.resource.resourcesystem.messaging;

import edu.br.resource.resourcesystem.observability.FailureDetails;
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
                log.debug("event=outbox_sent eventId={} batchId={}", id, token);
            }
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            for (var entry : confirmations.entrySet()) {
                var correlation = entry.getValue();
                var confirm = correlation.getFuture().get(Math.max(1, deadline - System.nanoTime()),
                        TimeUnit.NANOSECONDS);
                if (!confirm.ack() || correlation.getReturned() != null)
                    throw new IllegalStateException("Publicação não confirmada ou sem rota.");
                confirmed.add(entry.getKey());
                log.info("event=outbox_broker_confirmed eventId={} batchId={}", entry.getKey(), token);
            }
        } catch (Exception failure) {
            if (failure instanceof InterruptedException)
                Thread.currentThread().interrupt();
            log.warn("event=outbox_publish_retry batchId={} claimed={} confirmed={} failure={}",
                    token, ids.size(), confirmed.size(), FailureDetails.describe(failure));
        } finally {
            try {
                store.finish(ids, confirmed, token);
            } catch (RuntimeException failure) {
                log.error("event=outbox_finish_failed batchId={} claimed={} confirmed={} failure={}",
                        token, ids.size(), confirmed.size(), FailureDetails.describe(failure));
                throw failure;
            }
        }
    }
}
