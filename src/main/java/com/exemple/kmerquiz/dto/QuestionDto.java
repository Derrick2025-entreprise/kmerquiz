package com.exemple.kmerquiz.dto;


import java.time.LocalTime;
import java.util.List;

public record QuestionDto(
        Long idQuestion,
        String label1,
        String label2,
        String label3,
        LocalTime time,
        DifficultyDto difficulty,
        List<CategoryDto> categories,
        List<AnswerDto> responses
) {}
