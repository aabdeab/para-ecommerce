package com.ecommerce.controllers;

import com.ecommerce.API.JwtTokenService;
import com.ecommerce.DTOs.CreateUserDTO;
import com.ecommerce.DTOs.LoginRequestDTO;
import com.ecommerce.configurations.EncoderConfig;
import com.ecommerce.configurations.SecurityConfig;
import com.ecommerce.services.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, EncoderConfig.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @MockBean
    private UserDetailsManager userDetailsManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_shouldReturnOk() throws Exception {
        CreateUserDTO dto = new CreateUserDTO("john@example.com", "John", "Doe","066605825", "password123");
        String expectedToken = "jwt-token-registration-123";

        when(authService.registerUser(dto)).thenReturn(expectedToken);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedToken));
    }

    @Test
    void login_shouldReturnToken() throws Exception {
        LoginRequestDTO loginDTO = new LoginRequestDTO("john@example.com", "password123");
        String fakeToken = "jwt-token-123";
        when(authService.login(loginDTO)).thenReturn(fakeToken);
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string(fakeToken));
    }
}