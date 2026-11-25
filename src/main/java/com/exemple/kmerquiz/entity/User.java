package com.exemple.kmerquiz.entity;

import com.exemple.kmerquiz.entity.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor
@ToString
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUser;
    private String pseudo;
    @Column(unique = true)
    private String email;
    private String phone;
    private String avatar;
    @Enumerated(EnumType.STRING)
    private Role role;
    @ToString.Exclude // Partie 8.2 : jamais le mot de passe dans un toString()/log
    private String password;
    private LocalDate inscriptionDate;
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;
}
