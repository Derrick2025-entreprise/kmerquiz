package com.exemple.kmerquiz.controllers;

import com.exemple.kmerquiz.dto.QuestionDto;
import com.exemple.kmerquiz.dto.QuizDto;
import com.exemple.kmerquiz.dto.QuizQuestionsRequest;
import com.exemple.kmerquiz.dto.QuizRequest;
import com.exemple.kmerquiz.entity.enums.StatusQuiz;
import com.exemple.kmerquiz.services.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuizDto create(@Valid @RequestBody QuizRequest request) {
        return quizService.create(request);
    }

    @GetMapping
    public List<QuizDto> getAll(@RequestParam(required = false) StatusQuiz status,
                                @RequestParam(required = false) String title,
                                @RequestParam(required = false) Long creatorId) {
        return quizService.getAll(status, title, creatorId);
    }

    @GetMapping("/{id}")
    public QuizDto getById(@PathVariable Long id) {
        return quizService.getById(id);
    }

    /** Modification : permission quiz:update + être le créateur (ou admin). */
    @PutMapping("/{id}")
    public QuizDto update(@PathVariable Long id, @Valid @RequestBody QuizRequest request) {
        return quizService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        quizService.delete(id);
    }

    @GetMapping("/{id}/questions")
    public List<QuestionDto> getQuestions(@PathVariable Long id) {
        return quizService.getQuestions(id);
    }

    @PostMapping("/{id}/questions")
    public List<QuestionDto> addQuestions(@PathVariable Long id,
                                          @Valid @RequestBody QuizQuestionsRequest request) {
        return quizService.addQuestions(id, request);
    }

    @DeleteMapping("/{id}/questions/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeQuestion(@PathVariable Long id, @PathVariable Long questionId) {
        quizService.removeQuestion(id, questionId);
    }
}
