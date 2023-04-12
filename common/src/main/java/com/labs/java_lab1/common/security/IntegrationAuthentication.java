package com.labs.java_lab1.common.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * Данные {@link org.springframework.security.core.Authentication} по интеграционному взаимодейтсвию.
 * По сути, не задаётся ничего, кроме authenticated
 */
public class IntegrationAuthentication extends AbstractAuthenticationToken {

    public IntegrationAuthentication() {
        super(null);
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }
}
