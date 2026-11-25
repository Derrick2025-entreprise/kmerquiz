package com.exemple.kmerquiz.services;

import com.exemple.kmerquiz.dto.UserRegisterRequest;
import com.exemple.kmerquiz.dto.AuthResponse;
import com.exemple.kmerquiz.dto.LoginRequest;
import com.exemple.kmerquiz.dto.RefreshTokenRequest;
import com.exemple.kmerquiz.security.JwtService;
import com.exemple.kmerquiz.security.UserPrincipal;
import com.exemple.kmerquiz.security.UserPrincipalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserPrincipalService userPrincipalService;
    private final UserService userService;

    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        return buildResponse((UserPrincipal) auth.getPrincipal());
    }

    public AuthResponse register(UserRegisterRequest request) {
        userService.register(request);
        return login(new LoginRequest(request.email(), request.password()));
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        String token = request.refreshToken();
        if (!jwtService.validateToken(token) || !jwtService.isRefreshToken(token)) {
            throw new BadCredentialsException("Refresh token invalide ou expiré");
        }
        try {
            String email = jwtService.getUsernameFromToken(token);
            UserPrincipal user = (UserPrincipal) userPrincipalService.loadUserByUsername(email);
            if (!user.isEnabled() || !user.isAccountNonLocked()) {
                throw new BadCredentialsException("Compte désactivé");
            }
            return buildResponse(user);
        } catch (UsernameNotFoundException e) {
            throw new BadCredentialsException("Refresh token invalide ou expiré");
        }
    }

    private AuthResponse buildResponse(UserPrincipal user) {
        return new AuthResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user),
                "Bearer",
                jwtService.getAccessExpirationMs() / 1000,
                userService.findById(user.getIdUser()));
    }
}
