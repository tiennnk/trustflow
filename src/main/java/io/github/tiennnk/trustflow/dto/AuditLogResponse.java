package io.github.tiennnk.trustflow.dto;

import java.time.Instant;
import java.util.UUID;

import io.github.tiennnk.trustflow.entity.AuditAction;

public record AuditLogResponse(
        UUID id,
        UUID actorId,
        AuditAction action,
        String reason,
        Instant dtCreated
) {}
