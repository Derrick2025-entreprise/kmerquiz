package com.exemple.kmerquiz.entity;

import com.exemple.kmerquiz.entity.enums.StatusQuiz;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter @Setter @NoArgsConstructor
public class Quiz {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idQuiz;
    private String title;
    private LocalDate beginDate;
    private LocalDate endDate;
    private Double totalScore;
    private String description;
    private Integer nbreQuestion;
    private LocalTime time;
    private Double scoreMin;
    @Enumerated(EnumType.STRING)
    private StatusQuiz status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_creator")
    private User createdBy;
}