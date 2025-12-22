package com.twowheeler.auth_service.Dto.Response;

import com.twowheeler.auth_service.Enum.Roles;
import com.twowheeler.auth_service.Enum.Status;

public record UserResponse(
                String userId,
                String username,
                String email,
                String phoneNum,
                Roles role,
                Status status,
                Long createdAt) {
}
