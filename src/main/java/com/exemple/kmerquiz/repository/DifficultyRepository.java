package com.exemple.kmerquiz.repository;

import com.exemple.kmerquiz.entity.Difficulty;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DifficultyRepository extends JpaRepository<Difficulty, Long> {
    boolean existsByLabel(String label);
}
