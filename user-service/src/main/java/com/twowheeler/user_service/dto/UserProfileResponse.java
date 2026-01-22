package com.twowheeler.user_service.dto;

import com.twowheeler.user_service.enums.AccountType;
import com.twowheeler.user_service.enums.UserCategory;
import com.twowheeler.user_service.enums.UserRole;

public record UserProfileResponse(
        String userId,
        String name,
        String email,
        String phone,
        UserRole role,
        AccountType accountType,
        UserCategory userCategory
) {}

