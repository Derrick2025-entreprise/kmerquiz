package com.exemple.kmerquiz.services;

import com.exemple.kmerquiz.dto.CategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto create(CategoryDto dto);

    CategoryDto getById(Long id);

    List<CategoryDto> getAll(String name);

    CategoryDto update(Long id, CategoryDto dto);

    void delete(Long id);
}
