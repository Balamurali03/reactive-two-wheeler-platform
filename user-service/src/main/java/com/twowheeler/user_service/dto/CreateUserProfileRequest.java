package com.twowheeler.user_service.dto;

import com.twowheeler.user_service.enums.AccountType;
import com.twowheeler.user_service.enums.Status;
import com.twowheeler.user_service.enums.UserCategory;
import com.twowheeler.user_service.enums.Roles;

public record CreateUserProfileRequest(
        String name,
        String email,
        String phone,
        AccountType accountType,
        UserCategory userCategory,
        Roles role,
        Status status
) {}

