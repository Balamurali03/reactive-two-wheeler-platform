package com.twowheeler.user_service.dto;

public record CreateUserProfileRequest(
        String name,
        String email,
        String phone
) {}

