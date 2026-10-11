package edu.br.resource.resourcesystem.model.entity;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.SuperBuilder;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;

import java.time.Instant;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
public abstract class StatusHistory extends BaseEntity {
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
}
