package com.exemple.kmerquiz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record AnswerRequest(
        @NotBlank(message = "Le texte de la réponse est obligatoire") String text,
        @NotNull(message = "Indiquez si la réponse est correcte") Boolean isValid
) {}
