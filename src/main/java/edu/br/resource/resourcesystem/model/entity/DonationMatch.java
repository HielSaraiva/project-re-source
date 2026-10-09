package edu.br.resource.resourcesystem.model.entity;

import edu.br.resource.resourcesystem.model.enums.DonationStage;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "matches")
public class DonationMatch extends AuditedEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "donation_id", nullable = false)
    private Donation donation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "necessity_id", nullable = false)
    private Necessity necessity;

    @Column(name = "allocated_quantity", nullable = false)
    private Integer allocatedQuantity;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "match_status")
    private MatchStatus status = MatchStatus.PROPOSED;

    @Column(name = "acceptance_deadline", nullable = false)
    private Instant acceptanceDeadline;

    @Column(name = "proposal_message", nullable = true, columnDefinition = "text")
    private String proposalMessage;

    @Column(name = "accepted_at", nullable = true)
    private Instant acceptedAt;

    @Column(name = "cancelled_at", nullable = true)
    private Instant cancelledAt;

    @Column(name = "completed_at", nullable = true)
    private Instant completedAt;

    @Column(name = "delivery_method_deadline", nullable = true)
    private Instant deliveryMethodDeadline;

    @Column(name = "protocol", nullable = false, length = 40, unique = true)
    private String protocol;

    @Column(name = "rejected_at", nullable = true)
    private Instant rejectedAt;

    @Column(name = "rejection_reason", nullable = true, columnDefinition = "text")
    private String rejectionReason;

    @Column(name = "cancellation_reason", nullable = true, columnDefinition = "text")
    private String cancellationReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "expired_stage", nullable = true, length = 30)
    private DonationStage expiredStage;
}
