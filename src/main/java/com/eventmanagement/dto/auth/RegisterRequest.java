package com.eventmanagement.dto.auth;

import com.eventmanagement.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank
    @Size(min = 2, max = 100, message = "Name should between 2 & 100 characters")
    private String name;

    @NotBlank
    @Email(message = "Valid email is required")
    private String email;

    @NotBlank
    @Size(min = 6, message = "Password must atleast 6 characters long")
    private String password;

    private Role role = Role.USER;
}