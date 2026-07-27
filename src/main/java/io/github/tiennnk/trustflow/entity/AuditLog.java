package io.github.tiennnk.trustflow.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "audit_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "verification_request_id", nullable = false)
    private UUID verificationRequestId;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Column(length = 200)
    private String reason;

    @CreationTimestamp
    @Column(name = "dt_created", nullable = false, updatable = false)
    private Instant dtCreated;

    public AuditLog(UUID verificationRequestId, UUID actorId, AuditAction action, String reason) {
        this.verificationRequestId = verificationRequestId;
        this.actorId = actorId;
        this.action = action;
        this.reason = reason;
    }
}
