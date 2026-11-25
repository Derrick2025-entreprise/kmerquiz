package com.exemple.kmerquiz.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
public class ServiceLoggingAspect {

    @Value("${app.logging.slow-threshold-ms:500}")
    private long slowThresholdMs;

    // Dans ce projet, les services sont dans com.exemple.kmerquiz.services (et services.impl)
    @Pointcut("execution(public * com.exemple.kmerquiz.services..*(..))")
    void serviceMethods() {}

    @Around("serviceMethods()")
    public Object trace(ProceedingJoinPoint pjp) throws Throwable {
        String method = pjp.getSignature().getDeclaringType().getSimpleName() + "." + pjp.getSignature().getName();
        // On journalise les TYPES des arguments, pas leurs valeurs (cf. question 2)
        String argTypes = Arrays.stream(pjp.getArgs())
                .map(a -> a == null ? "null" : a.getClass().getSimpleName())
                .collect(Collectors.joining(", "));
        log.debug("--> {}({})", method, argTypes);
        long start = System.nanoTime();
        try {
            Object result = pjp.proceed();
            long ms = (System.nanoTime() - start) / 1_000_000;
            if (ms > slowThresholdMs) {
                log.warn("<-- {} LENT : {} ms (seuil {} ms)", method, ms, slowThresholdMs);
            } else {
                log.debug("<-- {} {} ms", method, ms);
            }
            return result;
        } catch (Throwable ex) {
            log.debug("<-- {} exception {} : {}", method, ex.getClass().getSimpleName(), ex.getMessage());
            throw ex; // l'erreur sera journalisée une seule fois par le handler
        }
    }
}
