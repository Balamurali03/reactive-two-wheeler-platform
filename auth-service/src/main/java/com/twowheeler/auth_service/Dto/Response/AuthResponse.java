package com.twowheeler.auth_service.Dto.Response;

public record AuthResponse(
    String userId,
    String role,
    String accessToken,
    String refreshToken
) {}
