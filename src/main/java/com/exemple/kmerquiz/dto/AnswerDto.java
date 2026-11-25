package com.exemple.kmerquiz.dto;

public record AnswerDto(
        Long idResponse,
        String text,
        Boolean isValid
) {}
