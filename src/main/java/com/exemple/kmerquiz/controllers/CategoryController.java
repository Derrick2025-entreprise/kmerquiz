package com.exemple.kmerquiz.controllers;

import com.exemple.kmerquiz.dto.CategoryDto;
import com.exemple.kmerquiz.services.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(@Valid @RequestBody CategoryDto dto) {
        return categoryService.create(dto);
    }

    @GetMapping
    public List<CategoryDto> getAll(@RequestParam(required = false) String name) {
        return categoryService.getAll(name);
    }

    @GetMapping("/{id}")
    public CategoryDto getById(@PathVariable Long id) {
        return categoryService.getById(id);
    }

    @PutMapping("/{id}")
    public CategoryDto update(@PathVariable Long id, @Valid @RequestBody CategoryDto dto) {
        return categoryService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }
}
