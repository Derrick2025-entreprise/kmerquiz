package com.exemple.kmerquiz.services.impl;

import com.exemple.kmerquiz.dto.CategoryDto;
import com.exemple.kmerquiz.entity.Category;
import com.exemple.kmerquiz.exceptions.DuplicateResourceException;
import com.exemple.kmerquiz.exceptions.ResourceNotFoundException;
import com.exemple.kmerquiz.mappers.CategoryMapper;
import com.exemple.kmerquiz.repository.CategoryRepository;
import com.exemple.kmerquiz.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDto create(CategoryDto dto) {
        if (categoryRepository.existsByName(dto.name())) {
            throw new DuplicateResourceException("Une catégorie existe déjà avec le nom : " + dto.name());
        }
        Category category = categoryMapper.toEntity(dto);
        category.setIdCategory(null);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public CategoryDto getById(Long id) {
        return categoryMapper.toDto(findOrThrow(id));
    }

    @Override
    public List<CategoryDto> getAll(String name) {
        List<Category> categories = name == null
                ? categoryRepository.findAll()
                : categoryRepository.findByNameContainingIgnoreCase(name);
        return categoryMapper.toDtoList(categories);
    }

    @Override
    @Transactional
    public CategoryDto update(Long id, CategoryDto dto) {
        Category category = findOrThrow(id);
        if (!category.getName().equalsIgnoreCase(dto.name())
                && categoryRepository.existsByName(dto.name())) {
            throw new DuplicateResourceException("Une catégorie existe déjà avec le nom : " + dto.name());
        }
        category.setName(dto.name());
        category.setDescription(dto.description());
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        categoryRepository.delete(findOrThrow(id));
    }

    private Category findOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée avec l'id : " + id));
    }
}
