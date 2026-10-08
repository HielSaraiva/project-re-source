package edu.br.resource.resourcesystem.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.br.resource.resourcesystem.model.enums.InstitutionSector;
import edu.br.resource.resourcesystem.model.enums.InstitutionStatus;
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
@Table(name = "institutions")
public class Institution extends AuditedEntity {
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "cnpj", nullable = false, length = 14, columnDefinition = "char(14)")
    private String cnpj;

    @Column(name = "legal_name", nullable = false, length = 200)
    private String legalName;

    @Column(name = "email", nullable = false, length = 320)
    private String email;

    @JsonIgnore
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "representative_full_name", nullable = false, length = 200)
    private String representativeFullName;

    @JsonIgnore
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "representative_cpf", nullable = false, length = 11, columnDefinition = "char(11)")
    private String representativeCpf;

    @Column(name = "description", nullable = true, columnDefinition = "text")
    private String description;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "sector", nullable = false, columnDefinition = "institution_sector")
    private InstitutionSector sector = InstitutionSector.OTHER;

    @Column(name = "logo_url", nullable = true, length = 500)
    private String logoUrl;

    @Column(name = "cover_image_url", nullable = true, length = 500)
    private String coverImageUrl;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "institution_status")
    private InstitutionStatus status = InstitutionStatus.PENDING_APPROVAL;

    @Column(name = "rejection_reason", nullable = true, columnDefinition = "text")
    private String rejectionReason;

    @Column(name = "reviewed_at", nullable = true)
    private Instant reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "reviewed_by_user_id", nullable = true)
    private User reviewedBy;

    @Column(name = "phone", nullable = true, length = 30)
    private String phone;
}
