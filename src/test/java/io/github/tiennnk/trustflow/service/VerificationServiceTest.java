package io.github.tiennnk.trustflow.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import io.github.tiennnk.trustflow.dto.VerificationResponse;
import io.github.tiennnk.trustflow.entity.VerificationRequest;
import io.github.tiennnk.trustflow.entity.VerificationStatus;
import io.github.tiennnk.trustflow.exception.CustomException;
import io.github.tiennnk.trustflow.repository.VerificationRequestRepository;

@ExtendWith(MockitoExtension.class)
class VerificationServiceTest {

    @Mock
    private VerificationRequestRepository verificationRequestRepository;

    @InjectMocks
    private VerificationService verificationService;

    @Test
    void submit_noPendingRequest_returnsPendingResponse() {
        UUID userId = UUID.randomUUID();

        when(verificationRequestRepository.existsByUserIdAndStatus(userId, VerificationStatus.PENDING))
                .thenReturn(false);

        VerificationResponse response = verificationService.submit(userId);

        assertEquals(VerificationStatus.PENDING, response.status());
        verify(verificationRequestRepository).saveAndFlush(any(VerificationRequest.class));
    }

    @Test
    void submit_alreadyPending_throwsConflict() {
        UUID userId = UUID.randomUUID();

        when(verificationRequestRepository.existsByUserIdAndStatus(userId, VerificationStatus.PENDING))
                .thenReturn(true);

        CustomException exception = assertThrows(CustomException.class, () -> verificationService.submit(userId));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(verificationRequestRepository, never()).saveAndFlush(any(VerificationRequest.class));
    }

    @Test
    void submit_raceCondition_throwsConflict() {
        UUID userId = UUID.randomUUID();

        when(verificationRequestRepository.existsByUserIdAndStatus(userId, VerificationStatus.PENDING))
                .thenReturn(false);
        when(verificationRequestRepository.saveAndFlush(any(VerificationRequest.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        CustomException exception = assertThrows(CustomException.class, () -> verificationService.submit(userId));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void getPendingRequests_returnsOnlyPending() {
        UUID userId = UUID.randomUUID();
        VerificationRequest request = new VerificationRequest(userId);

        when(verificationRequestRepository.findByStatusOrderByDtSubmittedAsc(VerificationStatus.PENDING))
                .thenReturn(List.of(request));

        List<VerificationResponse> responses = verificationService.getPendingRequests();

        assertEquals(1, responses.size());
        assertEquals(VerificationStatus.PENDING, responses.get(0).status());
    }

    @Test
    void approve_pendingRequest_returnsApprovedResponse() {
        UUID reviewerId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        VerificationRequest request = new VerificationRequest(UUID.randomUUID());

        when(verificationRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

        VerificationResponse response = verificationService.approve(reviewerId, requestId);

        assertEquals(VerificationStatus.APPROVED, response.status());
        assertEquals(reviewerId, response.reviewerId());
    }

    @Test
    void approve_alreadyReviewed_throwsConflict() {
        UUID reviewerId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        VerificationRequest request = new VerificationRequest(UUID.randomUUID());
        request.approve(UUID.randomUUID());

        when(verificationRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

        CustomException exception = assertThrows(CustomException.class,
                () -> verificationService.approve(reviewerId, requestId));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void approve_concurrentReview_throwsConflict() {
        UUID reviewerId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        VerificationRequest request = new VerificationRequest(UUID.randomUUID());

        when(verificationRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(verificationRequestRepository.saveAndFlush(any(VerificationRequest.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(VerificationRequest.class, requestId));

        CustomException exception = assertThrows(CustomException.class,
                () -> verificationService.approve(reviewerId, requestId));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void approve_notFound_throwsNotFound() {
        UUID requestId = UUID.randomUUID();

        when(verificationRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class,
                () -> verificationService.approve(UUID.randomUUID(), requestId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void reject_pendingRequest_returnsRejectedResponse() {
        UUID reviewerId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        VerificationRequest request = new VerificationRequest(UUID.randomUUID());

        when(verificationRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

        VerificationResponse response = verificationService.reject(reviewerId, requestId, "invalid document");

        assertEquals(VerificationStatus.REJECTED, response.status());
        assertEquals("invalid document", response.rejectionReason());
    }

    @Test
    void reject_alreadyReviewed_throwsConflict() {
        UUID reviewerId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        VerificationRequest request = new VerificationRequest(UUID.randomUUID());
        request.reject(UUID.randomUUID(), "invalid document");

        when(verificationRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

        CustomException exception = assertThrows(CustomException.class,
                () -> verificationService.reject(reviewerId, requestId, "invalid document"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void getDetail_owner_returnsResponse() {
        UUID userId = UUID.randomUUID();
        VerificationRequest request = new VerificationRequest(userId);

        when(verificationRequestRepository.findById(any())).thenReturn(Optional.of(request));

        VerificationResponse response = verificationService.getDetail(userId, UUID.randomUUID(), false);

        assertEquals(VerificationStatus.PENDING, response.status());
    }

    @Test
    void getDetail_notOwnerAndNotReviewer_throwsForbidden() {
        UUID ownerId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        VerificationRequest request = new VerificationRequest(ownerId);

        when(verificationRequestRepository.findById(any())).thenReturn(Optional.of(request));

        CustomException exception = assertThrows(CustomException.class,
                () -> verificationService.getDetail(requesterId, UUID.randomUUID(), false));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void getDetail_notOwnerButReviewer_returnsResponse() {
        UUID ownerId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();
        VerificationRequest request = new VerificationRequest(ownerId);

        when(verificationRequestRepository.findById(any())).thenReturn(Optional.of(request));

        VerificationResponse response = verificationService.getDetail(reviewerId, UUID.randomUUID(), true);

        assertEquals(VerificationStatus.PENDING, response.status());
    }

    @Test
    void getDetail_notFound_throwsNotFound() {
        when(verificationRequestRepository.findById(any())).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class,
                () -> verificationService.getDetail(UUID.randomUUID(), UUID.randomUUID(), false));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}
