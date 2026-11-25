package com.exemple.kmerquiz.controllers;

import com.exemple.kmerquiz.dto.UserRegisterRequest;
import com.exemple.kmerquiz.dto.UserResponse;
import com.exemple.kmerquiz.services.UserService;
import com.exemple.kmerquiz.security.CurrentUser;
import com.exemple.kmerquiz.security.Permission;
import com.exemple.kmerquiz.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody UserRegisterRequest request) {
        return userService.register(request);
    }

    @GetMapping
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable Long id) {

        UserPrincipal current = CurrentUser.get();
        boolean self = current != null && current.getIdUser().equals(id);
        boolean canReadUsers = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> Permission.USER_READ.getPermission().equals(a.getAuthority()));
        if (!self && !canReadUsers) {
            throw new AccessDeniedException("Accès refusé");
        }
        return userService.findById(id);
    }
}