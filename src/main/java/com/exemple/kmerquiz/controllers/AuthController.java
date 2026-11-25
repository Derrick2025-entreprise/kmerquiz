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
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody UserRegisterRequest request) {
        return authService.register(request);
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
