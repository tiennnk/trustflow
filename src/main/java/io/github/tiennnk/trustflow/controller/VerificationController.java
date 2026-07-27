package io.github.tiennnk.trustflow.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.tiennnk.trustflow.dto.AuditLogResponse;
import io.github.tiennnk.trustflow.dto.RejectRequest;
import io.github.tiennnk.trustflow.dto.VerificationResponse;
import io.github.tiennnk.trustflow.exception.CustomException;
import io.github.tiennnk.trustflow.service.AuditLogService;
import io.github.tiennnk.trustflow.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/verifications")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;
    private final AuditLogService auditLogService;

    @PostMapping
    public ResponseEntity<VerificationResponse> submit(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(verificationService.submit(userId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<VerificationResponse>> getOwnRequests(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(verificationService.getOwnRequests(userId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<VerificationResponse>> getPendingRequests(Authentication authentication) {
        requireReviewer(authentication);
        return ResponseEntity.ok(verificationService.getPendingRequests());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<VerificationResponse> approve(
            @AuthenticationPrincipal UUID reviewerId,
            Authentication authentication,
            @PathVariable UUID id) {

        requireReviewer(authentication);
        return ResponseEntity.ok(verificationService.approve(reviewerId, id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<VerificationResponse> reject(
            @AuthenticationPrincipal UUID reviewerId,
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody RejectRequest request) {

        requireReviewer(authentication);
        return ResponseEntity.ok(verificationService.reject(reviewerId, id, request.rejectionReason()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VerificationResponse> getDetail(
            @AuthenticationPrincipal UUID userId,
            Authentication authentication,
            @PathVariable UUID id) {

        return ResponseEntity.ok(verificationService.getDetail(userId, id, isReviewer(authentication)));
    }

    @GetMapping("/{id}/audit-logs")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs(
            Authentication authentication,
            @PathVariable UUID id) {

        requireReviewer(authentication);
        return ResponseEntity.ok(auditLogService.getLogs(id));
    }

    private void requireReviewer(Authentication authentication) {
        if (!isReviewer(authentication)) {
            throw new CustomException(HttpStatus.FORBIDDEN, "Only reviewers or admins can perform this action!");
        }
    }

    // reviewer/admin xem duoc chi tiet cac request, user chi duoc xem cua chinh usser
    private boolean isReviewer(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_REVIEWER")
                        || authority.getAuthority().equals("ROLE_ADMIN"));
    }
}
