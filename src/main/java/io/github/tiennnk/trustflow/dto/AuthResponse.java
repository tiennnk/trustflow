package io.github.tiennnk.trustflow.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds) {
}
