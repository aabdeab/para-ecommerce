package com.ecommerce.services;


import com.ecommerce.API.JwtTokenService;
import com.ecommerce.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class AuthServiceTest {

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    UserRepository userRepository;

    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    JwtTokenService jwtTokenService;

    @Test
    void register_user_with_empty_email(){

    }
    @Test
    void register_user_with_empty_password(){

    }
    @Test
    void register_user_with_empty_firstname(){}
    @Test
    void register_user_with_empty_lastname(){}
    @Test
    void register_user_with_correct_input(){}

    @Test
    void test_mapper(){}
}
