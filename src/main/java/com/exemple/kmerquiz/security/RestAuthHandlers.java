package com.exemple.kmerquiz.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


@Component
public class RestAuthHandlers {

    public AuthenticationEntryPoint entryPoint() {
        return (request, response, ex) ->
                write(response, HttpStatus.UNAUTHORIZED, "Authentification requise");
    }

    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) ->
                write(response, HttpStatus.FORBIDDEN, "Accès refusé : permission insuffisante");
    }

    private static void write(HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
