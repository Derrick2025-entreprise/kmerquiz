package com.exemple.kmerquiz.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Difficulty {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDifficulty;
    private String label;
}