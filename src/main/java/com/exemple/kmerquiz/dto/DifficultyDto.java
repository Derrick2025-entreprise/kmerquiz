package com.exemple.kmerquiz.dto;

import jakarta.validation.constraints.NotBlank;

public record DifficultyDto(
        Long idDifficulty,
        @NotBlank(message = "Le libellé de la difficulté est obligatoire") String label
) {
}
