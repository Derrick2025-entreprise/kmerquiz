package com.exemple.kmerquiz.mappers;

import com.exemple.kmerquiz.dto.CategoryDto;
import com.exemple.kmerquiz.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {
    CategoryDto toDto(Category category);

    Category toEntity(CategoryDto dto);

    List<CategoryDto> toDtoList(List<Category> categories);
}
