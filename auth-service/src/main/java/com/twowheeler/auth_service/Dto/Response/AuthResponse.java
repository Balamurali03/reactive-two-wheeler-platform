package com.twowheeler.auth_service.Dto.Response;

public record AuthResponse(
    String accessToken,
    String refreshToken
) {}
