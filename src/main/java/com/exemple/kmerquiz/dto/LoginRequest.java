package com.exemple.kmerquiz.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "L'email est obligatoire") @Email(message = "Email invalide") String email,
        @NotBlank(message = "Le mot de passe est obligatoire") String password
) {
    // Partie 6 : ne jamais exposer le mot de passe dans un log
    @Override
    public String toString() {
        return "LoginRequest[email=" + email + ", password=****]";
    }
}
