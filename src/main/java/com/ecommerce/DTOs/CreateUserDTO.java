package com.ecommerce.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserDTO(
        @Email(message = "Invalid email format")
        @NotBlank
        String email,

        @NotBlank(message = "firstname must not be empty")
        String firstname,

        @NotBlank(message = "lastname must not be empty")
        String lastname,

        String phone,

        @NotBlank
        String password


) {
}
