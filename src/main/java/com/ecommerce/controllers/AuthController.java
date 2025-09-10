package com.ecommerce.controllers;

import com.ecommerce.DTOs.ApiResponse;
import com.ecommerce.DTOs.CreateUserDTO;
import com.ecommerce.DTOs.LoginRequestDTO;
import com.ecommerce.services.AuthService;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@RequestBody @Valid CreateUserDTO dto,@Nullable String role) {
        String token;
        if(role==null){
            token = authService.registerUser(dto);
        } else {
            token = authService.registerUser(dto,role);
        }
        ApiResponse<String> response = ApiResponse.<String>builder()
                .data(token)
                .success(true)
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.ok(response) ;
    }
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody @Valid LoginRequestDTO loginRequest) {
        String token = authService.login(loginRequest);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .data(token)
                .success(true)
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.ok(response);
    }
}
