package com.ecommerce.controllers;

import com.ecommerce.DTOs.CreateUserDTO;
import com.ecommerce.DTOs.LoginRequestDTO;
import com.ecommerce.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid CreateUserDTO dto) {
        String token = authService.registerUser(dto);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid LoginRequestDTO loginRequest) {
        String token = authService.login(loginRequest);
        return ResponseEntity.ok(token);
    }
}
