package edu.br.resource.resourcesystem.model.entity;

import edu.br.resource.resourcesystem.model.enums.MatchEventType;
import edu.br.resource.resourcesystem.model.enums.MatchStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SourceType;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "match_status_history")
public class MatchStatusHistory extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private DonationMatch match;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "previous_status", nullable = true, columnDefinition = "match_status")
    private MatchStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "new_status", nullable = false, columnDefinition = "match_status")
    private MatchStatus newStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "changed_by_user_id", nullable = true)
    private User changedByUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "changed_by_institution_id", nullable = true)
    private Institution changedByInstitution;

    @Column(name = "notes", nullable = true, columnDefinition = "text")
    private String notes;

    @CreationTimestamp(source = SourceType.DB)
    @Setter(AccessLevel.NONE)
    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private MatchEventType eventType = MatchEventType.STATUS_CHANGED;

    @Column(name = "event_title", nullable = true, length = 200)
    private String eventTitle;

    @Column(name = "actor_name", nullable = true, length = 200)
    private String actorName;
}
