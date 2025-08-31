package com.ecommerce.DTOs;

import com.ecommerce.models.UserRole;

public record UserResponseDTO(
        Long userId,
        String email,
        String firstname,
        String lastname,
        UserRole role
) {
}
