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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.tiennnk.trustflow.dto.VerificationResponse;
import io.github.tiennnk.trustflow.service.VerificationService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/verifications")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    @PostMapping
    public ResponseEntity<VerificationResponse> submit(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(verificationService.submit(userId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<VerificationResponse>> getOwnRequests(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(verificationService.getOwnRequests(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VerificationResponse> getDetail(
            @AuthenticationPrincipal UUID userId,
            Authentication authentication,
            @PathVariable UUID id) {

        // reviewer/admin xem duoc chi tiet cac request, user chi duoc xem cua chinh usser
        boolean canViewAny = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_REVIEWER")
                        || authority.getAuthority().equals("ROLE_ADMIN"));

        return ResponseEntity.ok(verificationService.getDetail(userId, id, canViewAny));
    }
}
