package io.github.tiennnk.trustflow.dto;

import java.time.Instant;
import java.util.UUID;

import io.github.tiennnk.trustflow.entity.VerificationStatus;

public record VerificationResponse(
        UUID id,
        VerificationStatus status,
        Instant dtSubmitted,
        Instant dtReviewed,
        UUID reviewerId,
        String rejectionReason
) {}
