package com.exemple.kmerquiz.exceptions;

import com.exemple.kmerquiz.logging.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Partie 8.1 : chaque exception est journalisée UNE seule fois, ici, là où elle est traitée.
 * 4xx (erreur côté client) -> WARN ; erreur inattendue -> ERROR avec la pile d'appels.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException exception,
                                                              HttpServletRequest request) {
        log.warn("Ressource introuvable : {} uri={}", exception.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, String>> handleDuplicate(DuplicateResourceException exception,
                                                               HttpServletRequest request) {
        log.warn("Doublon : {} uri={}", exception.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleBadCredentials(RuntimeException exception,
                                                                    HttpServletRequest request) {
        // Le détail (qui, pourquoi) est déjà dans le journal d'audit (LOGIN_FAILURE)
        log.warn("Identifiants refusés ({}) uri={}", exception.getClass().getSimpleName(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Email ou mot de passe incorrect"));
    }

    @ExceptionHandler({DisabledException.class, LockedException.class})
    public ResponseEntity<Map<String, String>> handleAccountState(RuntimeException exception,
                                                                  HttpServletRequest request) {
        log.warn("Compte désactivé ou banni ({}) uri={}", exception.getClass().getSimpleName(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Compte désactivé ou banni"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException exception,
                                                                  HttpServletRequest request) {
        log.warn("Accès refusé : {} uri={}", exception.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Accès refusé : permission insuffisante"));
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, String>> handleBusinessRule(BusinessRuleException exception,
                                                                  HttpServletRequest request) {
        log.warn("Règle métier non respectée : {} uri={}", exception.getMessage(), request.getRequestURI());
        return ResponseEntity.badRequest()
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleIntegrity(DataIntegrityViolationException exception,
                                                               HttpServletRequest request) {
        log.warn("Contrainte d'intégrité violée uri={} cause={}", request.getRequestURI(),
                exception.getMostSpecificCause().getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", "Opération impossible : cette ressource est liée à d'autres données"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException exception,
                                                                HttpServletRequest request) {
        Map<String, String> errors = exception.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        fieldError -> fieldError.getDefaultMessage() == null
                                ? "Valeur invalide"
                                : fieldError.getDefaultMessage(),
                        (firstError, ignored) -> firstError
                ));
        // On journalise les NOMS des champs en erreur, pas les valeurs envoyées (mot de passe...)
        log.warn("Requête invalide champs={} uri={}", errors.keySet(), request.getRequestURI());
        return ResponseEntity.badRequest().body(Map.of(
                "message", "La requête contient des champs invalides",
                "errors", errors
        ));
    }

    // Utilisée par UserService (email déjà utilisé -> 409, utilisateur introuvable -> 404)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException exception,
                                                                    HttpServletRequest request) {
        log.warn("{} : {} uri={}", exception.getStatusCode().value(), exception.getReason(), request.getRequestURI());
        String reason = exception.getReason() == null ? "Erreur" : exception.getReason();
        return ResponseEntity.status(exception.getStatusCode()).body(Map.of("message", reason));
    }

    // Corps JSON illisible ou paramètre de mauvais type : erreur du client (400), pas une 500
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(Exception exception, HttpServletRequest request) {
        log.warn("Requête mal formée ({}) uri={}", exception.getClass().getSimpleName(), request.getRequestURI());
        return ResponseEntity.badRequest().body(Map.of("message", "Requête mal formée"));
    }

    // Handler générique (tâche 8.1)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception exception, HttpServletRequest request) {
        // Exceptions MVC standard qui portent déjà leur statut (404 route inconnue, 405 méthode...) : pas une 500
        if (exception instanceof ErrorResponse errorResponse) {
            log.warn("{} {} -> {} ({})", request.getMethod(), request.getRequestURI(),
                    errorResponse.getStatusCode().value(), exception.getClass().getSimpleName());
            return ResponseEntity.status(errorResponse.getStatusCode()).body(errorResponse.getBody());
        }
        String cid = MDC.get(CorrelationIdFilter.MDC_KEY);
        log.error("Erreur inattendue sur {} {}", request.getMethod(), request.getRequestURI(), exception);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erreur interne. Communiquez cet identifiant au support : " + cid);
        problem.setProperty("correlationId", cid);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
