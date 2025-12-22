package com.twowheeler.auth_service.Dto.Response;

import java.time.Instant;

public record ErrorResponse(
        Integer status,
        String error,
        String message,
        Instant timestamp
) {}
