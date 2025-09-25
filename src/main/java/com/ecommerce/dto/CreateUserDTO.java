package com.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserDTO(
        @Email(message = "Invalid email format")
        @NotBlank
        String email,

        @NotBlank(message = "firstname must not be empty")
        String firstname,

        @NotBlank(message = "lastname must not be empty")
        String lastname,

        String phone,

        @NotBlank(message = "password should not be empty")
        String password


) {
}
