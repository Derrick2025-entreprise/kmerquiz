package com.exemple.kmerquiz.controllers;

import com.exemple.kmerquiz.dto.QuestionDto;
import com.exemple.kmerquiz.dto.QuestionRequest;
import com.exemple.kmerquiz.services.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionDto create(@Valid @RequestBody QuestionRequest request) {
        return questionService.create(request);
    }

    @GetMapping
    public List<QuestionDto> getAll(@RequestParam(required = false) Long categoryId,
                                    @RequestParam(required = false) Long difficultyId) {
        return questionService.getAll(categoryId, difficultyId);
    }

    @GetMapping("/{id}")
    public QuestionDto getById(@PathVariable Long id) {
        return questionService.getById(id);
    }

    @PutMapping("/{id}")
    public QuestionDto update(@PathVariable Long id, @Valid @RequestBody QuestionRequest request) {
        return questionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        questionService.delete(id);
    }
}
