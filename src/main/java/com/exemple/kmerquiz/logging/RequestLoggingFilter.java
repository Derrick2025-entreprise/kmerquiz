package com.exemple.kmerquiz.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1) // juste après CorrelationIdFilter
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        long start = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            long ms = (System.nanoTime() - start) / 1_000_000;
            int status = response.getStatus();
            String query = request.getQueryString() == null ? "" : "?" + request.getQueryString();
            String msg = "HTTP {} {}{} -> {} ({} ms) ip={}";
            Object[] args = {request.getMethod(), request.getRequestURI(), query, status, ms, request.getRemoteAddr()};
            if (status >= 500)      log.error(msg, args);
            else if (status >= 400) log.warn(msg, args);
            else                    log.info(msg, args);
        }
    }
}
