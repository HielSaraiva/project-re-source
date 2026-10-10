package edu.br.resource.resourcesystem.repository;

import edu.br.resource.resourcesystem.model.entity.DonationMatch;
import edu.br.resource.resourcesystem.model.enums.MatchEventType;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import edu.br.resource.resourcesystem.observability.TransactionLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@RequiredArgsConstructor
public class MatchEventRepository {
    private final JdbcTemplate jdbc;
    private final edu.br.resource.resourcesystem.service.notification.NotificationService notifications;

    public void append(DonationMatch match, MatchEventType type) {
        UUID eventId = UUID.randomUUID();
        jdbc.update("""
            insert into match_event_outbox(id, match_id, protocol, event_type, donor_id, institution_id)
            values (?, ?, ?, ?, ?, ?)
            """, eventId, match.getId(), match.getProtocol(), type.getValue(),
                match.getDonation().getDonor().getId(), match.getNecessity().getInstitution().getId());
        notifications.record(eventId);
        TransactionLog.afterCommit(log, "event=outbox_created eventId={} protocol={} action={}",
                eventId, match.getProtocol(), type.getValue());
    }

    public List<UUID> claimPending(UUID token, int batchSize, int leaseSeconds) {
        return jdbc.query("""
            with candidates as (
                select id from match_event_outbox
                where published_at is null and next_attempt_at <= now()
                  and (claimed_until is null or claimed_until <= now())
                order by created_at, id limit ? for update skip locked
            )
            update match_event_outbox event
            set claim_token=?, claimed_until=now() + (? * interval '1 second'), attempts=attempts+1
            from candidates where event.id=candidates.id
            returning event.id
            """, (row, number) -> row.getObject("id", UUID.class), batchSize, token, leaseSeconds);
    }

    public void markPublished(List<UUID> ids, UUID token) {
        jdbc.batchUpdate("""
            update match_event_outbox
            set published_at=now(), claim_token=null, claimed_until=null
            where id=? and claim_token=? and published_at is null
            """, ids, 200, (statement, id) -> {
                statement.setObject(1, id); statement.setObject(2, token);
            });
    }

    public void scheduleRetry(List<UUID> ids, UUID token) {
        jdbc.batchUpdate("""
            update match_event_outbox
            set next_attempt_at=now() + (least(300, 30 * power(2, least(attempts-1, 4))) * interval '1 second'),
                claim_token=null, claimed_until=null
            where id=? and claim_token=? and published_at is null
            """, ids, 200, (statement, id) -> {
                statement.setObject(1, id); statement.setObject(2, token);
            });
    }

    public void recordNotification(UUID id) {
        int inserted = jdbc.update("""
            insert into match_event_notifications(event_id,match_id,event_type,donor_id,institution_id)
            select id,match_id,event_type,donor_id,institution_id from match_event_outbox where id=?
            on conflict(event_id) do nothing
            """, id);
        if (inserted == 0 && !Boolean.TRUE.equals(jdbc.queryForObject(
                "select exists(select 1 from match_event_outbox where id=?)", Boolean.class, id)))
            throw new IllegalArgumentException("Evento desconhecido.");
        notifications.record(id);
        jdbc.update("UPDATE user_notifications SET email_ready=true WHERE event_id=?", id);
        if (inserted > 0)
            TransactionLog.afterCommit(log, "event=notification_recorded eventId={}", id);
        else
            log.debug("event=notification_duplicate eventId={}", id);
    }
}
