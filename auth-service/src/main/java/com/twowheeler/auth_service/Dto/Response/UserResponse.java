package com.twowheeler.auth_service.Dto.Response;

import java.time.Instant;


import com.twowheeler.auth_service.Enum.Roles;

public record UserResponse(
                String userId,
                String username,
                Roles role,
                Instant createdAt) {
}
