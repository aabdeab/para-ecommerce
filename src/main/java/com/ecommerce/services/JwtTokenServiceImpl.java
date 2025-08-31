package com.ecommerce.services;

import com.ecommerce.API.JwtTokenService;
import com.ecommerce.models.SecurityUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JwtTokenServiceImpl implements JwtTokenService {
    private final long jwtExpirationInMs = 3600000;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public JwtTokenServiceImpl(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();
        String scope = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        SecurityUser currentUser = (SecurityUser) authentication.getPrincipal();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(currentUser.getUsername())
                .claim("userId", currentUser.getUserId())
                .claim("authorities",currentUser.getAuthorities())
                .claim("role", currentUser.user().getRole().name())
                .claim("scope", scope)
                .issuedAt(now)
                .expiresAt(now.plus(jwtExpirationInMs, ChronoUnit.MILLIS))
                .build();
        var jwtEncoderParameters = JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims);
        return this.jwtEncoder.encode(jwtEncoderParameters).getTokenValue();
    }

    @Override
    public String getUserFromToken(String token) {
        Jwt jwtToken = jwtDecoder.decode(token);
        return jwtToken.getSubject();
    }
    @Override
    public Long getUserIdFromToken(String token) {
        Jwt jwtToken = jwtDecoder.decode(token);
        return jwtToken.getClaim("userId");
    }
    public Collection<? extends GrantedAuthority> getAuthoritesFromToken(String token) {
        Jwt jwtToken = jwtDecoder.decode(token);
        return jwtToken.getClaim("authorities");
    }

    @Override
    public boolean validateToken(String token) {
        try {
            jwtDecoder.decode(token);
            return true;
        } catch (Exception exception) {
            log.error("[USER] : Error while trying to validate token", exception);
            throw new BadJwtException("Error while trying to validate token");
        }
    }
    @Override
    public String getRoleFromToken(String token) {
        Jwt jwtToken = jwtDecoder.decode(token);
        return jwtToken.getClaim("role");
    }
}