package com.exemple.kmerquiz.dto;

import com.exemple.kmerquiz.entity.enums.StatusQuiz;

import java.time.LocalDate;
import java.time.LocalTime;

public record QuizDto(
        Long idQuiz,
        String title,
        String description,
        LocalDate beginDate,
        LocalDate endDate,
        Double totalScore,
        Double scoreMin,
        Integer nbreQuestion,
        LocalTime time,
        StatusQuiz status,
        Long idCreator,
        String creatorPseudo,
        long questionCount
) {}
