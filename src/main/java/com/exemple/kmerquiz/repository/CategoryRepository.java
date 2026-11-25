package com.exemple.kmerquiz.repository;

import com.exemple.kmerquiz.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);

    List<Category> findByNameContainingIgnoreCase(String name);
}
