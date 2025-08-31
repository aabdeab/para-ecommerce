package com.ecommerce.services;


import com.ecommerce.API.JwtTokenService;
import com.ecommerce.DTOs.CreateUserDTO;
import com.ecommerce.DTOs.LoginRequestDTO;
import com.ecommerce.exceptions.UserNotFoundException;
import com.ecommerce.mappers.AuthMapper;
import com.ecommerce.models.User;
import com.ecommerce.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService tokenService;

    @Transactional
    public String registerUser(CreateUserDTO dto) {
        User user = AuthMapper.fromDTO(dto);
        user.setPassword(passwordEncoder.encode(dto.password()));
        userRepository.save(user);
        log.info("[USER] : User created with id {}", user.getUserId());
        return login(AuthMapper.fromDto(dto));
    }
    /**
     *
     * @param loginRequest
     * @return
     * @Throws BadCredentialsException si le le mot de passe ou email ne sont pas corrects
     */
    public String login(LoginRequestDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );
        log.info("[USER] : User Authenticated with email {}",authentication.getName());
        return tokenService.generateToken(authentication);
    }
}





