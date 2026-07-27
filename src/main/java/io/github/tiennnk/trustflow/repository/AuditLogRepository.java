package io.github.tiennnk.trustflow.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.tiennnk.trustflow.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findByVerificationRequestIdOrderByDtCreatedAsc(UUID verificationRequestId);
}
