package com.exemple.kmerquiz.repository;

import com.exemple.kmerquiz.entity.QuizQuestion;
import com.exemple.kmerquiz.entity.QuizQuestionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, QuizQuestionId> {

    @Query("select qq from QuizQuestion qq join fetch qq.question where qq.quiz.idQuiz = :idQuiz order by qq.question.idQuestion")
    List<QuizQuestion> findByQuizId(@Param("idQuiz") Long idQuiz);

    @Query("select count(qq) from QuizQuestion qq where qq.quiz.idQuiz = :idQuiz")
    long countByQuizId(@Param("idQuiz") Long idQuiz);

    @Modifying
    @Query("delete from QuizQuestion qq where qq.quiz.idQuiz = :idQuiz")
    void deleteByQuizId(@Param("idQuiz") Long idQuiz);

    @Modifying
    @Query("delete from QuizQuestion qq where qq.question.idQuestion = :idQuestion")
    void deleteByQuestionId(@Param("idQuestion") Long idQuestion);
}
