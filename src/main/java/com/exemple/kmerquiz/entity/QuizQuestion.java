
package com.exemple.kmerquiz.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class QuizQuestion {

    @EmbeddedId
    private QuizQuestionId id;

    private Boolean status;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idQuiz")
    @JoinColumn(name = "id_quiz")
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idQuestion")
    @JoinColumn(name = "id_question")
    private Question question;
}

