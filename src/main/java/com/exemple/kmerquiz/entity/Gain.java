package com.exemple.kmerquiz.entity;

import com.exemple.kmerquiz.entity.enums.TypeGain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Gain {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idGain;
    @Enumerated(EnumType.STRING)
    private TypeGain type;
    private Double scoreMin;
}