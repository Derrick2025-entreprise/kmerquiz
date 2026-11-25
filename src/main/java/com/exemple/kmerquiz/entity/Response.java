package com.exemple.kmerquiz.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Response {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idResponse;

    private String text;

    private Boolean isValid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_question")
    private Question question;
}
