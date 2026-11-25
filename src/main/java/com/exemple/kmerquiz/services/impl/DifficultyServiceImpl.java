package com.exemple.kmerquiz.services.impl;

import com.exemple.kmerquiz.dto.DifficultyDto;
import com.exemple.kmerquiz.entity.Difficulty;
import com.exemple.kmerquiz.exceptions.DuplicateResourceException;
import com.exemple.kmerquiz.exceptions.ResourceNotFoundException;
import com.exemple.kmerquiz.mappers.DifficultyMapper;
import com.exemple.kmerquiz.repository.DifficultyRepository;
import com.exemple.kmerquiz.services.DifficultyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DifficultyServiceImpl implements DifficultyService {

    private final DifficultyRepository difficultyRepository;
    private final DifficultyMapper difficultyMapper;

    @Override
    @Transactional
    public DifficultyDto create(DifficultyDto dto) {
        if (difficultyRepository.existsByLabel(dto.label())) {
            throw new DuplicateResourceException("Une difficulté existe déjà avec le libellé : " + dto.label());
        }
        Difficulty difficulty = difficultyMapper.toEntity(dto);
        difficulty.setIdDifficulty(null);
        return difficultyMapper.toDto(difficultyRepository.save(difficulty));
    }

    @Override
    public DifficultyDto getById(Long id) {
        return difficultyMapper.toDto(findOrThrow(id));
    }

    @Override
    public List<DifficultyDto> getAll() {
        return difficultyMapper.toDtoList(difficultyRepository.findAll());
    }

    @Override
    @Transactional
    public DifficultyDto update(Long id, DifficultyDto dto) {
        Difficulty difficulty = findOrThrow(id);
        if (!difficulty.getLabel().equalsIgnoreCase(dto.label())
                && difficultyRepository.existsByLabel(dto.label())) {
            throw new DuplicateResourceException("Une difficulté existe déjà avec le libellé : " + dto.label());
        }
        difficulty.setLabel(dto.label());
        return difficultyMapper.toDto(difficultyRepository.save(difficulty));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        difficultyRepository.delete(findOrThrow(id));
    }

    private Difficulty findOrThrow(Long id) {
        return difficultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Difficulté non trouvée avec l'id : " + id));
    }
}
