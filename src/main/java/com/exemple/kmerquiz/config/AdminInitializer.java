package com.exemple.kmerquiz.config;

import com.exemple.kmerquiz.entity.User;
import com.exemple.kmerquiz.entity.enums.Role;
import com.exemple.kmerquiz.entity.UserStatus;
import com.exemple.kmerquiz.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.pseudo}")
    private String pseudo;
    @Value("${app.admin.email}")
    private String email;
    @Value("${app.admin.password}")
    private String password;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(email)) {
            return;
        }
        Role adminRole = Arrays.stream(Role.values())
                .filter(r -> r.name().equals("ADMIN"))
                .findFirst()
                .orElse(null);
        if (adminRole == null) {
            log.warn("L'enum Role ne contient pas ADMIN : compte administrateur non créé");
            return;
        }
        User admin = new User();
        admin.setPseudo(pseudo);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(adminRole);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setInscriptionDate(LocalDate.now());
        userRepository.save(admin);
        log.info("Compte administrateur créé : {}", email);
    }
}
