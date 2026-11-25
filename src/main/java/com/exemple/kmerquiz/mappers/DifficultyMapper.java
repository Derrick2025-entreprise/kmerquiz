package com.exemple.kmerquiz.mappers;

import com.exemple.kmerquiz.dto.DifficultyDto;
import com.exemple.kmerquiz.entity.Difficulty;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DifficultyMapper {
    DifficultyDto toDto(Difficulty difficulty);

    Difficulty toEntity(DifficultyDto dto);

    List<DifficultyDto> toDtoList(List<Difficulty> difficulties);
}
