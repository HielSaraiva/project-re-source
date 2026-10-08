package edu.br.resource.resourcesystem.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "open_days")
public class OpenDays extends BaseEntity {
    @Column(name = "monday", nullable = false)
    private boolean monday;

    @Column(name = "tuesday", nullable = false)
    private boolean tuesday;

    @Column(name = "wednesday", nullable = false)
    private boolean wednesday;

    @Column(name = "thursday", nullable = false)
    private boolean thursday;

    @Column(name = "friday", nullable = false)
    private boolean friday;

    @Column(name = "saturday", nullable = false)
    private boolean saturday;

    @Column(name = "sunday", nullable = false)
    private boolean sunday;
}
