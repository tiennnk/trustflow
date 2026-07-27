package io.github.tiennnk.trustflow.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.tiennnk.trustflow.dto.AuditLogResponse;
import io.github.tiennnk.trustflow.entity.AuditAction;
import io.github.tiennnk.trustflow.entity.AuditLog;
import io.github.tiennnk.trustflow.repository.AuditLogRepository;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @Test
    void record_savesAuditLog() {
        UUID requestId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        auditLogService.record(requestId, actorId, AuditAction.SUBMITTED, null);

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void getLogs_returnsLogsInOrder() {
        UUID requestId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        AuditLog log = new AuditLog(requestId, actorId, AuditAction.APPROVED, null);

        when(auditLogRepository.findByVerificationRequestIdOrderByDtCreatedAsc(requestId))
                .thenReturn(List.of(log));

        List<AuditLogResponse> responses = auditLogService.getLogs(requestId);

        assertEquals(1, responses.size());
        assertEquals(AuditAction.APPROVED, responses.get(0).action());
        assertEquals(actorId, responses.get(0).actorId());
    }
}
