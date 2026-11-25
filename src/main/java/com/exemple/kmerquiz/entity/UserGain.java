
package com.exemple.kmerquiz.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserGain {

    @EmbeddedId
    private UserGainId id;

    private LocalDate gettingDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idGain")
    @JoinColumn(name = "id_gain")
    private Gain gain;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idUser")
    @JoinColumn(name = "id_user")
    private User user;
}

