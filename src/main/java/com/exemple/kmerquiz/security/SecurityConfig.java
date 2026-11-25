package com.exemple.kmerquiz.security;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static com.exemple.kmerquiz.security.Permission.*;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtService jwtService,
                                           UserPrincipalService userPrincipalService,
                                           RestAuthHandlers handlers) throws Exception {
        JwtFilter jwtFilter = new JwtFilter(jwtService, userPrincipalService);

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ---------- Routes publiques ----------
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        .requestMatchers("/error").permitAll()

                        // ---------- Actuator (Partie 8.4) ----------
                        .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        // ---------- Authentification ----------
                        .requestMatchers("/api/auth/me").authenticated()

                        // ---------- Utilisateurs ----------
                        .requestMatchers(HttpMethod.GET, "/api/users").hasAuthority(USER_READ.getPermission())
                        // /api/users/{id} : soi-même ou user:read (vérifié dans UserController)
                        .requestMatchers(HttpMethod.GET, "/api/users/*").authenticated()

                        // ---------- Catégories ----------
                        .requestMatchers(HttpMethod.GET, "/api/categories", "/api/categories/**").hasAuthority(CATEGORY_READ.getPermission())
                        .requestMatchers(HttpMethod.POST, "/api/categories").hasAuthority(CATEGORY_CREATE.getPermission())
                        .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasAuthority(CATEGORY_UPDATE.getPermission())
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasAuthority(CATEGORY_DELETE.getPermission())

                        // ---------- Difficultés ----------
                        .requestMatchers(HttpMethod.GET, "/api/difficulties", "/api/difficulties/**").authenticated()
                        .requestMatchers("/api/difficulties", "/api/difficulties/**").hasRole("ADMIN")

                        // ---------- Questions ----------
                        .requestMatchers(HttpMethod.GET, "/api/questions", "/api/questions/**").hasAuthority(QUESTION_READ.getPermission())
                        .requestMatchers(HttpMethod.POST, "/api/questions").hasAuthority(QUESTION_CREATE.getPermission())
                        .requestMatchers(HttpMethod.PUT, "/api/questions/**").hasAuthority(QUESTION_UPDATE.getPermission())
                        .requestMatchers(HttpMethod.DELETE, "/api/questions/**").hasAuthority(QUESTION_DELETE.getPermission())

                        // ---------- Quiz ----------
                        .requestMatchers(HttpMethod.GET, "/api/quizzes", "/api/quizzes/**").hasAuthority(QUIZ_READ.getPermission())
                        // gérer les questions d'un quiz = modifier le quiz (avant la règle DELETE générale)
                        .requestMatchers(HttpMethod.POST, "/api/quizzes/*/questions").hasAuthority(QUIZ_UPDATE.getPermission())
                        .requestMatchers(HttpMethod.DELETE, "/api/quizzes/*/questions/*").hasAuthority(QUIZ_UPDATE.getPermission())
                        .requestMatchers(HttpMethod.POST, "/api/quizzes").hasAuthority(QUIZ_CREATE.getPermission())
                        .requestMatchers(HttpMethod.PUT, "/api/quizzes/**").hasAuthority(QUIZ_UPDATE.getPermission())
                        .requestMatchers(HttpMethod.DELETE, "/api/quizzes/**").hasAuthority(QUIZ_DELETE.getPermission())

                        // ---------- Tout le reste ----------
                        .anyRequest().authenticated())

                // Tâche 7.3 : accès non authentifié à une ressource protégée -> audit + 401
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            AUDIT.warn("UNAUTHENTICATED_ACCESS uri={} ip={}", request.getRequestURI(), request.getRemoteAddr());
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                        })
                        .accessDeniedHandler(handlers.accessDeniedHandler()))  // 403
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserPrincipalService userPrincipalService,
                                                       PasswordEncoder passwordEncoder,
                                                       AuthenticationEventPublisher authenticationEventPublisher) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userPrincipalService);
        provider.setPasswordEncoder(passwordEncoder);
        ProviderManager manager = new ProviderManager(provider);
        // Sans cela, notre ProviderManager ne publie aucun événement (LOGIN_SUCCESS / LOGIN_FAILURE)
        manager.setAuthenticationEventPublisher(authenticationEventPublisher);
        return manager;
    }

    // Tâche 7.1 : publie AuthenticationSuccessEvent / AbstractAuthenticationFailureEvent
    @Bean
    public AuthenticationEventPublisher authenticationEventPublisher(ApplicationEventPublisher publisher) {
        return new DefaultAuthenticationEventPublisher(publisher);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins}") String allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(",")).map(String::trim).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
