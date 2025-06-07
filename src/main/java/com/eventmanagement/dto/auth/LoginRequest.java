package com.eventmanagement.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequest {

    @NotBlank
    @Email(message = "Valid email is required")
    private String email;

    @NotBlank
    private String password;
}