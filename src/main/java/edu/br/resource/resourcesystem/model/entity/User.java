package edu.br.resource.resourcesystem.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.br.resource.resourcesystem.model.enums.AccountRole;
import edu.br.resource.resourcesystem.model.enums.AccountStatus;
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
@Table(name = "users")
public class User extends AuditedEntity {
    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @JsonIgnore
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "cpf", nullable = true, length = 11, columnDefinition = "char(11)")
    private String cpf;

    @Column(name = "email", nullable = false, length = 320)
    private String email;

    @JsonIgnore
    @Column(name = "password_hash", nullable = true, length = 255)
    private String passwordHash;

    @Column(name = "oauth_provider", nullable = true, length = 50)
    private String oauthProvider;

    @JsonIgnore
    @Column(name = "oauth_subject", nullable = true, length = 255)
    private String oauthSubject;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "role", nullable = false, columnDefinition = "account_role")
    private AccountRole role = AccountRole.DONOR;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "account_status")
    private AccountStatus status = AccountStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "city_id", nullable = true)
    private City city;

    @Column(name = "last_login_at", nullable = true)
    private Instant lastLoginAt;
}
