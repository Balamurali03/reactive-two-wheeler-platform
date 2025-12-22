package com.twowheeler.auth_service.Dto.Request;

import com.twowheeler.auth_service.Enum.Roles;
import jakarta.validation.constraints.*;

public record RegisterRequest(
                @NotBlank(message = "Username is required") String username,

                @NotBlank(message = "Password is required") String password,

                @NotNull(message = "Role is required") Roles role,

                @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,

                @NotBlank(message = "Phone number is required") @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone number must be a valid 10-digit mobile number") String phoneNumber) 
                
{}
