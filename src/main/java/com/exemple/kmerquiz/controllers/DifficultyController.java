package com.exemple.kmerquiz.controllers;

import com.exemple.kmerquiz.dto.DifficultyDto;
import com.exemple.kmerquiz.services.DifficultyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/difficulties")
@RequiredArgsConstructor
public class DifficultyController {

    private final DifficultyService difficultyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DifficultyDto create(@Valid @RequestBody DifficultyDto dto) {
        return difficultyService.create(dto);
    }

    @GetMapping
    public List<DifficultyDto> getAll() {
        return difficultyService.getAll();
    }

    @GetMapping("/{id}")
    public DifficultyDto getById(@PathVariable Long id) {
        return difficultyService.getById(id);
    }

    @PutMapping("/{id}")
    public DifficultyDto update(@PathVariable Long id, @Valid @RequestBody DifficultyDto dto) {
        return difficultyService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        difficultyService.delete(id);
    }
}
