package com.exemple.kmerquiz.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

public record QuestionRequest(
        @NotBlank(message = "Le libellé de la question est obligatoire") String label1,
        String label2,
        String label3,
        LocalTime time,
        Long idDifficulty,
        Set<Long> categoryIds,
        @NotNull(message = "Les réponses sont obligatoires")
        @Size(min = 2, message = "Une question doit avoir au moins 2 réponses")
        List<@Valid AnswerRequest> responses
) {}
