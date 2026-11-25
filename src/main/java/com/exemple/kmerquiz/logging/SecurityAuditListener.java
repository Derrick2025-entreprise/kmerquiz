package com.exemple.kmerquiz.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class SecurityAuditListener {
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        AUDIT.info("LOGIN_SUCCESS user={}", event.getAuthentication().getName());
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        AUDIT.warn("LOGIN_FAILURE user={} raison={}",
                event.getAuthentication().getName(),
                event.getException().getClass().getSimpleName());
    }
}
