package com.exemple.kmerquiz.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter @NoArgsConstructor
public class Question {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idQuestion;
    private String label1;
    private String label2;
    private String label3;
    private LocalTime time;

    @ManyToOne
    @JoinColumn(name = "id_difficulty")
    private Difficulty difficulty;

    @ManyToMany
    @JoinTable(name = "category_question",
            joinColumns = @JoinColumn(name = "id_question"),
            inverseJoinColumns = @JoinColumn(name = "id_category"))
    private Set<Category> categories = new HashSet<>();
}