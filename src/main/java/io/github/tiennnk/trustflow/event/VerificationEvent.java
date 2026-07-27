package io.github.tiennnk.trustflow.event;

import java.time.Instant;
import java.util.UUID;

import io.github.tiennnk.trustflow.entity.AuditAction;

public record VerificationEvent(
        UUID verificationRequestId,
        UUID actorId,
        AuditAction action,
        String reason,
        Instant dtOccurred
) {

    public VerificationEvent(UUID verificationRequestId, UUID actorId, AuditAction action, String reason) {
        this(verificationRequestId, actorId, action, reason, Instant.now());
    }
}
