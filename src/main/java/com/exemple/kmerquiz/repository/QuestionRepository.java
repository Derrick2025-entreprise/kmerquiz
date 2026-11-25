package com.exemple.kmerquiz.repository;

import com.exemple.kmerquiz.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("""
            select distinct q from Question q
            left join q.categories c
            where (:categoryId is null or c.idCategory = :categoryId)
              and (:difficultyId is null or q.difficulty.idDifficulty = :difficultyId)
            order by q.idQuestion
            """)
    List<Question> search(@Param("categoryId") Long categoryId,
                          @Param("difficultyId") Long difficultyId);
}
