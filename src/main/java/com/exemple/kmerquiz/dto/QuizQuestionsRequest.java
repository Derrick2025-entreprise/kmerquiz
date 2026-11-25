package com.exemple.kmerquiz.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record QuizQuestionsRequest(
        @NotEmpty(message = "Au moins une question est requise") List<Long> questionIds
) {}
