
package com.exemple.kmerquiz.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class QuizQuestionId implements Serializable {

    private Long idQuiz;

    private Long idQuestion;
}

