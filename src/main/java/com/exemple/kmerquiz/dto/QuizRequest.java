package com.exemple.kmerquiz.dto;

import com.exemple.kmerquiz.entity.enums.StatusQuiz;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;
import java.time.LocalTime;

public record QuizRequest(
        @NotBlank(message = "Le titre du quiz est obligatoire") String title,
        String description,
        LocalDate beginDate,
        LocalDate endDate,
        @PositiveOrZero(message = "Le score total doit être positif") Double totalScore,
        @PositiveOrZero(message = "Le score minimum doit être positif") Double scoreMin,
        @Positive(message = "Le nombre de questions doit être supérieur à 0") Integer nbreQuestion,
        LocalTime time,
        StatusQuiz status
) {}
