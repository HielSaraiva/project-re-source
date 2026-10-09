package edu.br.resource.resourcesystem.model.entity;

import edu.br.resource.resourcesystem.model.enums.DeliveryMethod;
import edu.br.resource.resourcesystem.model.enums.DeliveryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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
@Table(name = "deliveries")
public class Delivery extends AuditedEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false, unique = true)
    private DonationMatch match;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "method", nullable = false, columnDefinition = "delivery_method")
    private DeliveryMethod method;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "delivery_status")
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @Column(name = "pickup_address", nullable = true, columnDefinition = "text")
    private String pickupAddress;

    @Column(name = "delivery_address", nullable = true, columnDefinition = "text")
    private String deliveryAddress;

    @Column(name = "carrier_name", nullable = true, length = 150)
    private String carrierName;

    @Column(name = "tracking_code", nullable = true, length = 150)
    private String trackingCode;

    @Column(name = "scheduled_at", nullable = true)
    private Instant scheduledAt;

    @Column(name = "shipped_at", nullable = true)
    private Instant shippedAt;

    @Column(name = "delivered_at", nullable = true)
    private Instant deliveredAt;

    @Column(name = "in_person_deadline", nullable = true)
    private Instant inPersonDeadline;

    @Column(name = "method_confirmed_at", nullable = true)
    private Instant methodConfirmedAt;

    @Column(name = "received_by_name", nullable = true, length = 200)
    private String receivedByName;

    @Column(name = "delivery_notes", nullable = true, columnDefinition = "text")
    private String deliveryNotes;

    @Column(name = "reported_at", nullable = true)
    private Instant reportedAt;

    @Column(name = "receipt_confirmed_at", nullable = true)
    private Instant receiptConfirmedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "receipt_confirmed_by_institution_id", nullable = true)
    private Institution receiptConfirmedBy;

    @Column(name = "shipping_deadline", nullable = true)
    private Instant shippingDeadline;
}
