package io.github.tiennnk.trustflow.service;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.tiennnk.trustflow.dto.VerificationResponse;
import io.github.tiennnk.trustflow.entity.VerificationRequest;
import io.github.tiennnk.trustflow.entity.VerificationStatus;
import io.github.tiennnk.trustflow.exception.CustomException;
import io.github.tiennnk.trustflow.repository.VerificationRequestRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final VerificationRequestRepository verificationRequestRepository;

    @Transactional
    public VerificationResponse submit(UUID userId) {
        if (verificationRequestRepository.existsByUserIdAndStatus(userId, VerificationStatus.PENDING)) {
            throw new CustomException(HttpStatus.CONFLICT, "You already have a pending verification request!");
        }

        VerificationRequest request = new VerificationRequest(userId);

        try {
            verificationRequestRepository.saveAndFlush(request);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(HttpStatus.CONFLICT, "You already have a pending verification request!");
        }

        return toResponse(request);
    }

    public List<VerificationResponse> getOwnRequests(UUID userId) {
        return verificationRequestRepository.findByUserIdOrderByDtSubmittedDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<VerificationResponse> getPendingRequests() {
        return verificationRequestRepository.findByStatusOrderByDtSubmittedAsc(VerificationStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public VerificationResponse approve(UUID reviewerId, UUID requestId) {
        VerificationRequest request = getPendingOrThrow(requestId);
        request.approve(reviewerId);
        return saveReviewed(request);
    }

    @Transactional
    public VerificationResponse reject(UUID reviewerId, UUID requestId, String reason) {
        VerificationRequest request = getPendingOrThrow(requestId);
        request.reject(reviewerId, reason);
        return saveReviewed(request);
    }

    public VerificationResponse getDetail(UUID userId, UUID requestId, boolean canViewAny) {
        VerificationRequest request = verificationRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Verification request not found!"));

        if (!canViewAny && !request.getUserId().equals(userId)) {
            throw new CustomException(HttpStatus.FORBIDDEN, "You are not allowed to view this request!");
        }

        return toResponse(request);
    }

    private VerificationRequest getPendingOrThrow(UUID requestId) {
        VerificationRequest request = verificationRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Verification request not found!"));

        if (request.getStatus() != VerificationStatus.PENDING) {
            throw new CustomException(HttpStatus.CONFLICT, "This request has already been reviewed!");
        }

        return request;
    }

    private VerificationResponse saveReviewed(VerificationRequest request) {
        try {
            verificationRequestRepository.saveAndFlush(request);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new CustomException(HttpStatus.CONFLICT, "This request has already been reviewed by someone else!");
        }

        return toResponse(request);
    }

    private VerificationResponse toResponse(VerificationRequest request) {
        return new VerificationResponse(
                request.getId(),
                request.getStatus(),
                request.getDtSubmitted(),
                request.getDtReviewed(),
                request.getReviewerId(),
                request.getRejectionReason());
    }
}
