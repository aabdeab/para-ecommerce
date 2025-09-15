package com.ecommerce.controllers;

import com.ecommerce.API.JwtTokenService;
import com.ecommerce.dto.CreateUserDTO;
import com.ecommerce.dto.LoginRequestDTO;
import com.ecommerce.services.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
//I disabled filters for unit testing my controller
@AutoConfigureMockMvc(addFilters = false)
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
        CreateUserDTO dto = new CreateUserDTO("john@example.com", "John", "Doe", "066605825", "password123");
        String expectedToken = "jwt-token-registration-123";

        when(authService.registerUser(dto)).thenReturn(expectedToken);

        String response = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> map = objectMapper.readValue(response, Map.class);
        assertEquals(expectedToken, map.get("data"));
        assertEquals(true, map.get("success"));
    }

    @Test
    void login_shouldReturnToken() throws Exception {
        LoginRequestDTO loginDTO = new LoginRequestDTO("john@example.com", "password123");
        String fakeToken = "jwt-token-123";

        when(authService.login(loginDTO)).thenReturn(fakeToken);

        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> map = objectMapper.readValue(response, Map.class);
        assertEquals(fakeToken, map.get("data"));
        assertEquals(true, map.get("success"));
    }
}
