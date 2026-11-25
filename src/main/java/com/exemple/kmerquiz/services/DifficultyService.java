package com.exemple.kmerquiz.services;

import com.exemple.kmerquiz.dto.DifficultyDto;

import java.util.List;

public interface DifficultyService {
    DifficultyDto create(DifficultyDto dto);

    DifficultyDto getById(Long id);

    List<DifficultyDto> getAll();

    DifficultyDto update(Long id, DifficultyDto dto);

    void delete(Long id);
}
