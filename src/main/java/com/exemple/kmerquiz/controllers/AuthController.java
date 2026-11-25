package com.exemple.kmerquiz.controllers;

import com.exemple.kmerquiz.dto.UserRegisterRequest;
import com.exemple.kmerquiz.dto.UserResponse;
import com.exemple.kmerquiz.dto.AuthResponse;
import com.exemple.kmerquiz.dto.LoginRequest;
import com.exemple.kmerquiz.dto.RefreshTokenRequest;
import com.exemple.kmerquiz.security.UserPrincipal;
import com.exemple.kmerquiz.services.AuthService;
import com.exemple.kmerquiz.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody UserRegisterRequest request) {
        AuthResponse response = authService.register(request);
        AUDIT.info("REGISTER user={} role={}", response.user().email(), response.user().role());
        return response;
    }

    /**
     * Déconnexion : avec JWT (stateless) le serveur ne garde pas de session ;
     * le client supprime ses jetons. On trace l'événement dans le journal d'audit.
     */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal UserPrincipal principal) {
        AUDIT.info("LOGOUT user={}", principal.getUsername());
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request);
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        return userService.findById(principal.getIdUser());
    }
}
