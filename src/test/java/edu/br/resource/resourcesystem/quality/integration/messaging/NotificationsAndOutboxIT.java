package edu.br.resource.resourcesystem.quality.integration.messaging;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import edu.br.resource.resourcesystem.config.*;
import edu.br.resource.resourcesystem.messaging.*;
import edu.br.resource.resourcesystem.model.enums.*;
import edu.br.resource.resourcesystem.quality.support.WorkflowSupport;
import edu.br.resource.resourcesystem.repository.*;
import edu.br.resource.resourcesystem.service.mail.*;
import edu.br.resource.resourcesystem.service.notification.NotificationService;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.*;
import java.util.*;

@DisplayName("RN-015/RN-020 — outbox real, destinatários, leitura e falhas de envio")
class NotificationsAndOutboxIT extends WorkflowSupport {
    @Autowired NotificationService notifications;
    @Autowired MatchEventRepository events;
    @Autowired MatchOutboxPublisher publisher;
    @Autowired MatchOutboxStore store;
    @Autowired MatchEventConsumer consumer;
    @Autowired RabbitTemplate rabbit;
    @Autowired TransactionalMailDispatcher dispatcher;
    @Autowired TransactionalMailService mail;
    @Autowired UserRepository users;
    @Autowired PasswordRecoveryProperties recoverySettings;

    UUID event(String protocol, String type) {
        return db.queryForObject(
                "select id from match_event_outbox where protocol=? and event_type=? order by"
                        + " created_at desc limit 1",
                UUID.class,
                protocol,
                type);
    }

    @ParameterizedTest
    @CsvSource({
        "proposal_submitted,0,1",
        "proposal_accepted,1,0",
        "proposal_rejected,1,0",
        "delivery_method_selected,0,1",
        "delivery_reported,0,1",
        "shipment_reported,0,1",
        "receipt_confirmed,1,0",
        "donation_cancelled,1,1",
        "status_changed,1,1"
    })
    void notify_WithBusinessEvent_UsesOnlyExpectedRecipients(
            String type, int donorCount, int institutionCount) {
        var p = propose(4);
        UUID id = UUID.randomUUID();
        db.update(
                "insert into"
                    + " match_event_outbox(id,match_id,protocol,event_type,donor_id,institution_id)"
                    + " values(?,?,?,?,?,?)",
                id,
                p.id(),
                p.protocol(),
                type,
                donor,
                ong);
        notifications.record(id);
        assertThat(
                        db.queryForObject(
                                "select count(*) from user_notifications where event_id=? and"
                                        + " user_id=?",
                                Long.class,
                                id,
                                donor))
                .isEqualTo(donorCount);
        assertThat(
                        db.queryForObject(
                                "select count(*) from user_notifications where event_id=? and"
                                        + " institution_id=?",
                                Long.class,
                                id,
                                ong))
                .isEqualTo(institutionCount);
        notifications.record(id);
        assertThat(
                        db.queryForObject(
                                "select count(*) from user_notifications where event_id=?",
                                Long.class,
                                id))
                .isEqualTo(donorCount + institutionCount);
    }

    @Test
    void notify_WithFullyReceivedNeed_AlsoNotifiesInstitution() {
        var p = propose(10);
        workflow.accept(ong, p.protocol());
        workflow.selectMethod(donor, p.protocol(), DeliveryMethod.IN_PERSON);
        workflow.reportDelivery(
                donor,
                p.protocol(),
                new edu.br.resource.resourcesystem.dto.request.ConfirmInPersonDeliveryRequest(
                        today(), "Pessoa", null, true));
        workflow.receive(ong, p.protocol());
        UUID id = event(p.protocol(), "receipt_confirmed");
        assertThat(
                        db.queryForObject(
                                "select count(*) from user_notifications where event_id=?",
                                Long.class,
                                id))
                .isEqualTo(2);
    }

    @Test
    void publish_WithRealBroker_SendsOnlyUuidAndRecordsConfirmation() {
        var p = propose(4);
        UUID id = event(p.protocol(), "proposal_submitted");
        publisher.publishPending();
        var message = rabbit.receive(MessagingConfiguration.QUEUE, 5000);
        assertThat(message).isNotNull();
        assertThat(new String(message.getBody(), java.nio.charset.StandardCharsets.UTF_8))
                .isEqualTo(id.toString());
        assertThat(
                        db.queryForObject(
                                "select published_at is not null from match_event_outbox where"
                                        + " id=?",
                                Boolean.class,
                                id))
                .isTrue();
    }

    @Test
    void consume_WithActualListenerAndDuplicateEvent_ReleasesMailExactlyOnce() {
        var p = propose(4);
        UUID id = event(p.protocol(), "proposal_submitted");
        assertThat(count("user_notifications")).isEqualTo(1);
        assertThat(count("match_event_notifications")).isZero();
        publisher.publishPending();
        listeners.start();
        try {
            Awaitility.await()
                    .atMost(Duration.ofSeconds(10))
                    .untilAsserted(
                            () -> assertThat(count("match_event_notifications")).isEqualTo(1));
        } finally {
            listeners.stop();
        }
        consumer.recordNotification(id.toString());
        consumer.recordNotification(id.toString());
        assertThat(count("match_event_notifications")).isEqualTo(1);
        assertThat(count("user_notifications")).isEqualTo(1);
        assertThat(count("transactional_mail_outbox")).isEqualTo(2);
        assertThat(
                        db.queryForObject(
                                "select email_ready from user_notifications where event_id=?",
                                Boolean.class,
                                id))
                .isTrue();
    }

    @Test
    void consume_WithMalformedMessage_RoutesToDeadLetterQueue() {
        listeners.start();
        try {
            rabbit.convertAndSend(MessagingConfiguration.QUEUE, "invalid-uuid");
            Awaitility.await()
                    .atMost(Duration.ofSeconds(10))
                    .untilAsserted(
                            () ->
                                    assertThat(
                                                    ((Number)
                                                                    rabbitAdmin
                                                                            .getQueueProperties(
                                                                                    MessagingConfiguration
                                                                                                    .QUEUE
                                                                                            + ".dead")
                                                                            .get(
                                                                                    "QUEUE_MESSAGE_COUNT"))
                                                            .longValue())
                                            .isEqualTo(1L));
        } finally {
            listeners.stop();
        }
    }

    @Test
    void outbox_WithConcurrentClaims_DoesNotGiveSameLeaseToTwoWorkers() throws Exception {
        stage(Stage.ACCEPTED);
        var gate = new java.util.concurrent.CountDownLatch(1);
        try (var pool = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var a =
                    pool.submit(
                            () -> {
                                gate.await();
                                return store.claim(UUID.randomUUID(), 1, 60);
                            });
            var b =
                    pool.submit(
                            () -> {
                                gate.await();
                                return store.claim(UUID.randomUUID(), 1, 60);
                            });
            gate.countDown();
            var first = a.get();
            var second = b.get();
            assertThat(first).hasSize(1);
            assertThat(second).hasSize(1);
            assertThat(first).doesNotContainAnyElementsOf(second);
        }
    }

    @Test
    void outbox_WithWrongLeaseToken_DoesNotMarkPublished() {
        propose(1);
        UUID token = UUID.randomUUID();
        var ids = store.claim(token, 10, 60);
        store.finish(ids, ids, UUID.randomUUID());
        assertThat(
                        db.queryForObject(
                                "select count(*) from match_event_outbox where published_at is not"
                                        + " null",
                                Long.class))
                .isZero();
        store.finish(ids, List.of(), token);
        assertThat(
                        db.queryForObject(
                                "select count(*) from match_event_outbox where claim_token is null"
                                        + " and next_attempt_at>now()",
                                Long.class))
                .isEqualTo(1);
    }

    @Test
    void read_WithWrongOwner_HidesNotification() {
        propose(1);
        UUID id = db.queryForObject("select id from user_notifications", UUID.class);
        assertThatThrownBy(
                        () -> notifications.read(new NotificationService.Owner(false, donor), id))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        assertThat(notifications.inbox(new NotificationService.Owner(true, otherOng), 0).items())
                .isEmpty();
        notifications.read(new NotificationService.Owner(true, ong), id);
        assertThat(notifications.inbox(new NotificationService.Owner(true, ong), 0).unreadCount())
                .isZero();
        notifications.read(new NotificationService.Owner(true, ong), id);
    }

    @Test
    void inbox_WithMoreThanTwenty_NoticesPaginatesWithoutMarkingRead() {
        for (int i = 0; i < 22; i++) {
            int n = needFor(ong, type(), 10);
            workflow.propose(
                    donor,
                    new edu.br.resource.resourcesystem.dto.request.SubmitProposalRequest(
                            n, donation, 1, null));
            if (i % 9 == 8) inventoryAfterCancelAll();
        }
        var owner = new NotificationService.Owner(true, ong);
        assertThat(notifications.inbox(owner, -1).page()).isZero();
        assertThat(notifications.inbox(owner, 0).items()).hasSize(20);
        assertThat(notifications.inbox(owner, 0).hasMore()).isTrue();
        assertThat(notifications.inbox(owner, 1).items()).isNotEmpty();
        assertThat(notifications.inbox(owner, 0).unreadCount()).isGreaterThan(20);
        notifications.readAll(owner);
        assertThat(notifications.inbox(owner, 0).unreadCount()).isZero();
        assertThat(notifications.inbox(owner, Integer.MAX_VALUE).page()).isEqualTo(10000);
    }

    void inventoryAfterCancelAll() {
        for (String p :
                db.queryForList(
                        "select protocol from matches where status='awaiting_acceptance'",
                        String.class)) workflow.cancel(donor, p);
    }

    @Test
    void dispatch_WithFakeTransport_RendersBrandedMailAndAcknowledgesRow() throws Exception {
        transaction.executeWithoutResult(tx -> mail.welcome(users.findById(donor).orElseThrow()));
        dispatcher.deliver();
        assertThat(outgoing).hasSize(1);
        assertThat(outgoing.getFirst().getAllRecipients()[0].toString())
                .isEqualTo("donor@example.test");
        assertThat(outgoing.getFirst().getSubject()).contains("ReSource");
        assertThat(
                        db.queryForObject(
                                "select count(*) from transactional_mail_outbox where email_sent_at"
                                        + " is not null",
                                Long.class))
                .isEqualTo(1);
        dispatcher.deliver();
        assertThat(outgoing).hasSize(1);
    }

    @Test
    void dispatch_WithSmtpFailure_SchedulesRetryWithoutUndoingBusiness() {
        transaction.executeWithoutResult(tx -> mail.welcome(users.findById(donor).orElseThrow()));
        doThrow(new MailSendException("fictitious transport failure"))
                .when(mailSender)
                .send(any(MimeMessagePreparator.class));
        dispatcher.deliver();
        assertThat(
                        db.queryForObject(
                                "select email_attempts from transactional_mail_outbox",
                                Integer.class))
                .isEqualTo(1);
        assertThat(
                        db.queryForObject(
                                "select email_next_attempt_at>now() from transactional_mail_outbox",
                                Boolean.class))
                .isTrue();
        assertThat(count("users")).isEqualTo(2);
        assertThat(outgoing).isEmpty();
    }

    @Test
    void dispatch_WithEightFailedAttempts_StopsRetrying() {
        transaction.executeWithoutResult(tx -> mail.welcome(users.findById(donor).orElseThrow()));
        db.update("update transactional_mail_outbox set email_attempts=8");
        dispatcher.deliver();
        assertThat(outgoing).isEmpty();
    }

    @Test
    void dispatch_WithNotificationsDisabled_StillSendsSecurityMessage() {
        transaction.executeWithoutResult(tx -> mail.welcome(users.findById(donor).orElseThrow()));
        transaction.executeWithoutResult(tx -> mail.passwordChanged(donor, false, "test-hash"));
        ReflectionTestUtils.setField(dispatcher, "enabled", false);
        try {
            dispatcher.deliver();
            assertThat(outgoing).hasSize(1);
            assertThat(outgoing.getFirst().getSubject()).contains("senha");
        } catch (jakarta.mail.MessagingException e) {
            throw new AssertionError(e);
        } finally {
            ReflectionTestUtils.setField(dispatcher, "enabled", true);
        }
    }
}
