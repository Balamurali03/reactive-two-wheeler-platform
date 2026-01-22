package com.twowheeler.auth_service.Dto.Request;

import com.twowheeler.auth_service.Enum.Roles;
import jakarta.validation.constraints.*;

public record RegisterRequest(
                @NotBlank(message = "Username is required") String username,

                @NotBlank(message = "Password is required") String password,

                @NotNull(message = "Role is required") Roles role )
{}
