package com.ecommerce.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Builder
public record LoginRequestDTO(
        @Email
        @NotBlank
        String email,

        @NotBlank
        String password
) {
}
