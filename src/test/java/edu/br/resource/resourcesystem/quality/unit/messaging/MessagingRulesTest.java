package edu.br.resource.resourcesystem.quality.unit.messaging;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import edu.br.resource.resourcesystem.config.MessagingConfiguration;
import edu.br.resource.resourcesystem.messaging.*;
import edu.br.resource.resourcesystem.repository.*;
import edu.br.resource.resourcesystem.service.match.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.amqp.*;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

@DisplayName("RF-016/RF-022 — processamento assíncrono, confirmação e retry")
class MessagingRulesTest {
    @Nested
    @DisplayName("Publicação confirmada antes de marcar a outbox")
    class Publisher {
        MatchOutboxStore store = mock(MatchOutboxStore.class);
        RabbitTemplate rabbit = mock(RabbitTemplate.class);
        MatchOutboxPublisher sut = new MatchOutboxPublisher(store, rabbit);
        UUID first = UUID.randomUUID(), second = UUID.randomUUID();

        @BeforeEach
        void defaults() {
            ReflectionTestUtils.setField(sut, "batchSize", 50);
        }

        @Test
        void publish_WithNoEvents_DoesNotContactBroker() {
            when(store.claim(any(), anyInt(), anyInt())).thenReturn(List.of());
            sut.publishPending();
            verifyNoInteractions(rabbit);
            verify(store).finish(eq(List.of()), eq(List.of()), any());
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 0, 1, 50, 201})
        void publish_WithBatchSetting_ClampsLeaseBatch(int size) {
            ReflectionTestUtils.setField(sut, "batchSize", size);
            when(store.claim(any(), anyInt(), anyInt())).thenReturn(List.of());
            sut.publishPending();
            verify(store).claim(any(), eq(Math.max(1, Math.min(size, 200))), eq(60));
        }

        @Test
        void publish_WithAck_SendsPersistentUuidAndMarksConfirmed() {
            when(store.claim(any(), anyInt(), anyInt())).thenReturn(List.of(first));
            doAnswer(
                            i -> {
                                Message message = i.getArgument(2);
                                assertThat(
                                                new String(
                                                        message.getBody(),
                                                        java.nio.charset.StandardCharsets.UTF_8))
                                        .isEqualTo(first.toString());
                                assertThat(message.getMessageProperties().getDeliveryMode())
                                        .isEqualTo(MessageDeliveryMode.PERSISTENT);
                                assertThat(message.getMessageProperties().getMessageId())
                                        .isEqualTo(first.toString());
                                ((CorrelationData) i.getArgument(3))
                                        .getFuture()
                                        .complete(new CorrelationData.Confirm(true, null));
                                return null;
                            })
                    .when(rabbit)
                    .send(
                            eq(MessagingConfiguration.EXCHANGE),
                            eq(MessagingConfiguration.ROUTING_KEY),
                            any(Message.class),
                            any(CorrelationData.class));
            sut.publishPending();
            verify(store).finish(eq(List.of(first)), eq(List.of(first)), any());
        }

        @ParameterizedTest
        @ValueSource(strings = {"nack", "unroutable", "exception", "sendFailure"})
        void publish_WithBrokerFailure_LeavesEventForRetry(String mode) {
            when(store.claim(any(), anyInt(), anyInt())).thenReturn(List.of(first));
            doAnswer(
                            i -> {
                                if (mode.equals("sendFailure"))
                                    throw new AmqpConnectException(
                                            new java.io.IOException("fictitious"));
                                CorrelationData data = i.getArgument(3);
                                if (mode.equals("unroutable"))
                                    data.setReturned(
                                            new ReturnedMessage(
                                                    new Message(new byte[0]),
                                                    312,
                                                    "NO_ROUTE",
                                                    "e",
                                                    "r"));
                                if (mode.equals("exception"))
                                    data.getFuture()
                                            .completeExceptionally(
                                                    new IllegalStateException("fictitious"));
                                else
                                    data.getFuture()
                                            .complete(
                                                    new CorrelationData.Confirm(
                                                            !mode.equals("nack"), null));
                                return null;
                            })
                    .when(rabbit)
                    .send(anyString(), anyString(), any(Message.class), any(CorrelationData.class));
            sut.publishPending();
            verify(store).finish(eq(List.of(first)), eq(List.of()), any());
        }

        @Test
        void publish_WithInterruptedWait_PreservesInterruptForCaller() {
            when(store.claim(any(), anyInt(), anyInt())).thenReturn(List.of(first));
            doAnswer(
                            i -> {
                                Thread.currentThread().interrupt();
                                return null;
                            })
                    .when(rabbit)
                    .send(anyString(), anyString(), any(Message.class), any(CorrelationData.class));
            try {
                sut.publishPending();
                assertThat(Thread.currentThread().isInterrupted()).isTrue();
            } finally {
                Thread.interrupted();
            }
        }

        @Test
        void publish_WithFinishFailure_PropagatesWithoutPretendingSuccess() {
            when(store.claim(any(), anyInt(), anyInt())).thenReturn(List.of());
            doThrow(new IllegalStateException("fictitious"))
                    .when(store)
                    .finish(any(), any(), any());
            assertThatIllegalStateException().isThrownBy(sut::publishPending);
        }
    }

    @Nested
    @DisplayName("Consumo de identificadores")
    class Consumer {
        MatchEventRepository events = mock(MatchEventRepository.class);
        MatchEventConsumer sut = new MatchEventConsumer(events);

        @Test
        void consume_WithValidUuid_DelegatesPersistentProcessing() {
            UUID id = UUID.randomUUID();
            sut.recordNotification(id.toString());
            verify(events).recordNotification(id);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "invalid", "not-a-uuid"})
        void consume_WithMalformedUuid_RejectsWithoutRequeue(String input) {
            assertThatThrownBy(() -> sut.recordNotification(input))
                    .isInstanceOf(AmqpRejectAndDontRequeueException.class);
            verifyNoInteractions(events);
        }

        @Test
        void consume_WithUnknownEvent_RejectsWithoutRequeue() {
            UUID id = UUID.randomUUID();
            doThrow(new IllegalArgumentException("unknown")).when(events).recordNotification(id);
            assertThatThrownBy(() -> sut.recordNotification(id.toString()))
                    .isInstanceOf(AmqpRejectAndDontRequeueException.class);
        }

        @Test
        void consume_WithTransientDatabaseFailure_PropagatesForConfiguredRetry() {
            UUID id = UUID.randomUUID();
            doThrow(new IllegalStateException("temporary")).when(events).recordNotification(id);
            assertThatIllegalStateException()
                    .isThrownBy(() -> sut.recordNotification(id.toString()));
        }
    }

    @Test
    void finish_WithPartiallyConfirmedBatch_RetriesOnlyUnconfirmed() {
        var repo = mock(MatchEventRepository.class);
        UUID a = UUID.randomUUID(), b = UUID.randomUUID(), token = UUID.randomUUID();
        new MatchOutboxStore(repo).finish(List.of(a, b), List.of(a), token);
        verify(repo).markPublished(List.of(a), token);
        verify(repo).scheduleRetry(List.of(b), token);
    }

    @Test
    void claim_WithLease_PassesOwnershipToken() {
        var repo = mock(MatchEventRepository.class);
        UUID token = UUID.randomUUID();
        when(repo.claimPending(token, 10, 60)).thenReturn(List.of(token));
        assertThat(new MatchOutboxStore(repo).claim(token, 10, 60)).containsExactly(token);
    }

    @Test
    void expire_WithOneFailure_ContinuesOtherProtocols() {
        var repo = mock(MatchDeadlineRepository.class);
        var flow = mock(MatchWorkflowService.class);
        when(repo.findExpiredProtocols()).thenReturn(List.of("INT-2026-1", "INT-2026-2"));
        when(flow.expire("INT-2026-1")).thenThrow(new IllegalStateException("fictitious"));
        new MatchDeadlineScheduler(repo, flow).cancelExpired();
        verify(flow).expire("INT-2026-2");
    }

    @Test
    void append_WithEvent_RecordsThroughMandatoryTransactionAdapter() {
        var repo = mock(MatchEventRepository.class);
        var match = edu.br.resource.resourcesystem.model.entity.DonationMatch.builder().build();
        new MatchEventOutbox(repo)
                .append(
                        match,
                        edu.br.resource.resourcesystem.model.enums.MatchEventType
                                .PROPOSAL_SUBMITTED);
        verify(repo)
                .append(
                        match,
                        edu.br.resource.resourcesystem.model.enums.MatchEventType
                                .PROPOSAL_SUBMITTED);
    }
}
