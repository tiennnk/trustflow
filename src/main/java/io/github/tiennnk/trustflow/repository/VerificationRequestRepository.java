package io.github.tiennnk.trustflow.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.tiennnk.trustflow.entity.VerificationRequest;
import io.github.tiennnk.trustflow.entity.VerificationStatus;

public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, UUID> {

    boolean existsByUserIdAndStatus(UUID userId, VerificationStatus status);

    List<VerificationRequest> findByUserIdOrderByDtSubmittedDesc(UUID userId);
}
