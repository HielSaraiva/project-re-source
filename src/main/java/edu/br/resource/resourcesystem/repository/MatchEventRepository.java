package edu.br.resource.resourcesystem.repository;

import edu.br.resource.resourcesystem.model.entity.DonationMatch;
import edu.br.resource.resourcesystem.model.enums.MatchEventType;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MatchEventRepository {
    private final JdbcTemplate jdbc;

    public void append(DonationMatch match, MatchEventType type) {
        jdbc.update("""
            insert into match_event_outbox(id, match_id, protocol, event_type, donor_id, institution_id)
            values (?, ?, ?, ?, ?, ?)
            """, UUID.randomUUID(), match.getId(), match.getProtocol(), type.getValue(),
                match.getDonation().getDonor().getId(), match.getNecessity().getInstitution().getId());
    }

    public List<UUID> pendingForUpdate() {
        return jdbc.query("""
            select id from match_event_outbox
            where published_at is null and next_attempt_at <= now()
            order by created_at limit 10 for update skip locked
            """, (row, number) -> row.getObject("id", UUID.class));
    }

    public void markPublished(UUID id) {
        jdbc.update("update match_event_outbox set published_at=now(), attempts=attempts+1 where id=?", id);
    }

    public void scheduleRetry(UUID id) {
        jdbc.update("""
            update match_event_outbox set attempts=attempts+1,
            next_attempt_at=now()+interval '30 seconds' where id=?
            """, id);
    }

    public void recordNotification(UUID id) {
        Integer exists = jdbc.queryForObject("select count(*) from match_event_outbox where id=?", Integer.class, id);
        if (exists == null || exists == 0) throw new IllegalArgumentException("Evento desconhecido.");
        jdbc.update("""
            insert into match_event_notifications(event_id,match_id,event_type,donor_id,institution_id)
            select id,match_id,event_type,donor_id,institution_id from match_event_outbox where id=?
            on conflict(event_id) do nothing
            """, id);
    }
}
