package com.ecommerce.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserDTO(
        @Email(message = "Invalid email format")
        @NotBlank
        String email,

        @NotBlank
        String firstname,

        @NotBlank
        String lastname,

        String phone,

        @NotBlank
        String password


) {
}
