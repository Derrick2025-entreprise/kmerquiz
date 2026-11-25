
package com.exemple.kmerquiz.entity;

import com.exemple.kmerquiz.entity.enums.TypeRessource;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Ressource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRessource;

    @Enumerated(EnumType.STRING)
    private TypeRessource type;

    private String url;

    private String description;

    private String extension;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_gain")
    private Gain gain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_question")
    private Question question;
}

