package io.github.tiennnk.trustflow.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import io.github.tiennnk.trustflow.dto.AuditLogResponse;
import io.github.tiennnk.trustflow.entity.AuditAction;
import io.github.tiennnk.trustflow.entity.AuditLog;
import io.github.tiennnk.trustflow.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void record(UUID verificationRequestId, UUID actorId, AuditAction action, String reason) {
        auditLogRepository.save(new AuditLog(verificationRequestId, actorId, action, reason));
    }

    public List<AuditLogResponse> getLogs(UUID verificationRequestId) {
        return auditLogRepository.findByVerificationRequestIdOrderByDtCreatedAsc(verificationRequestId)
                .stream()
                .map(log -> new AuditLogResponse(
                        log.getId(),
                        log.getActorId(),
                        log.getAction(),
                        log.getReason(),
                        log.getDtCreated()))
                .toList();
    }
}
