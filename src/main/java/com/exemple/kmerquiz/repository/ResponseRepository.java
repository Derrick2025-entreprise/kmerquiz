package com.exemple.kmerquiz.repository;

import com.exemple.kmerquiz.entity.Response;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResponseRepository extends JpaRepository<Response, Long> {

    List<Response> findByQuestionIdQuestionOrderByIdResponse(Long idQuestion);

    @Modifying
    @Query("delete from Response r where r.question.idQuestion = :idQuestion")
    void deleteByQuestionId(@Param("idQuestion") Long idQuestion);
}
