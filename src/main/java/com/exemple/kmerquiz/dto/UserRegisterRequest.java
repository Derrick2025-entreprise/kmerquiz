package com.exemple.kmerquiz.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest(
        @NotBlank String pseudo,
        @NotBlank @Email String email,
        String phone,
        String avatar,
        @NotBlank @Size(min = 6) String password
) {
    // Partie 6 : ne jamais exposer le mot de passe dans un log
    @Override
    public String toString() {
        return "UserRegisterRequest[pseudo=" + pseudo + ", email=" + email
                + ", phone=" + phone + ", avatar=" + avatar + ", password=****]";
    }
}