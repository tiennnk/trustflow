package io.github.tiennnk.trustflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectRequest(
        @NotBlank
        @Size(max = 200)
        String rejectionReason
) {}
