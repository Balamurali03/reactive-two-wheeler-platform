package com.twowheeler.user_service.dto;

public record UserProfileResponse(
        String userId,
        String name,
        String email,
        String phone,
        String role
) {}

