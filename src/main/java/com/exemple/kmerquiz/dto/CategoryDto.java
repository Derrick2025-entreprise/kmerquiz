package com.exemple.kmerquiz.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryDto(
        Long idCategory,
        @NotBlank(message = "Le nom de la catégorie est obligatoire") String name,
        String description
) {
}
