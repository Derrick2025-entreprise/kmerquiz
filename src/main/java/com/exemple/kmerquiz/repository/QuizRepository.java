package com.exemple.kmerquiz.repository;

import com.exemple.kmerquiz.entity.Quiz;
import com.exemple.kmerquiz.entity.enums.StatusQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @Query("""
            select q from Quiz q
            where (:status is null or q.status = :status)
              and (:title is null or lower(q.title) like lower(concat('%', :title, '%')))
              and (:creatorId is null or q.createdBy.idUser = :creatorId)
            order by q.idQuiz
            """)
    List<Quiz> search(@Param("status") StatusQuiz status,
                      @Param("title") String title,
                      @Param("creatorId") Long creatorId);
}
