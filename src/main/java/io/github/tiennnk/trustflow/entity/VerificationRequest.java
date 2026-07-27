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
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "verification_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VerificationRequest {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus status;

    @Column(name = "reviewer_id")
    private UUID reviewerId;

    @Column(name = "rejection_reason", length = 200)
    private String rejectionReason;

    @CreationTimestamp
    @Column(name = "dt_submitted", nullable = false, updatable = false)
    private Instant dtSubmitted;

    @Column(name = "dt_reviewed")
    private Instant dtReviewed;

    @Version
    private Long version;

    public VerificationRequest(UUID userId) {
        this.userId = userId;
        this.status = VerificationStatus.PENDING;
    }
}
