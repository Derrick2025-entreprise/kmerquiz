package com.exemple.kmerquiz.dto;

import com.exemple.kmerquiz.entity.enums.Role;

import java.time.LocalDate;

public record UserResponse(
        Long idUser,
        String pseudo,
        String email,
        String phone,
        String avatar,
        Role role,
        LocalDate inscriptionDate
) {}