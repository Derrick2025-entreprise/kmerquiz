
package com.exemple.kmerquiz.entity;

import com.exemple.kmerquiz.entity.enums.StatusPart;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Part {

    @Id
    private String codePart;

    private LocalDate createDate;

    @Enumerated(EnumType.STRING)
    private StatusPart status;

    private Integer maxPlayers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_quiz")
    private Quiz quiz;
}
