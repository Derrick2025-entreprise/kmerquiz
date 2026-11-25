package com.exemple.kmerquiz.services;

import com.exemple.kmerquiz.dto.UserRegisterRequest;
import com.exemple.kmerquiz.dto.UserResponse;
import com.exemple.kmerquiz.entity.User;
import com.exemple.kmerquiz.entity.enums.Role;
import com.exemple.kmerquiz.repository.UserRepository;
import com.exemple.kmerquiz.utils.LogMasker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse register(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet email est déjà utilisé");
        }
        User user = new User();
        user.setPseudo(request.pseudo());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setAvatar(request.avatar());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.PLAYER);
        user.setInscriptionDate(LocalDate.now());
        User saved = userRepository.save(user);
        // jamais le mot de passe dans les logs
        log.info("Utilisateur créé idUser={} role={} pseudo={} email={} phone={}",
                saved.getIdUser(), saved.getRole(), saved.getPseudo(),
                LogMasker.maskEmail(saved.getEmail()), LogMasker.maskPhone(saved.getPhone()));
        return toResponse(saved);
    }

    public List<UserResponse> findAll() {
        List<UserResponse> users = userRepository.findAll().stream().map(this::toResponse).toList();
        log.debug("Liste des utilisateurs -> {} résultat(s)", users.size());
        return users;
    }

    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        return toResponse(user);
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getIdUser(), u.getPseudo(), u.getEmail(),
                u.getPhone(), u.getAvatar(), u.getRole(), u.getInscriptionDate());
    }
}