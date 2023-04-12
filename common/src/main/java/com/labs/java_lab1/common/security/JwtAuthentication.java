package com.labs.java_lab1.common.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * Данные {@link org.springframework.security.core.Authentication} по запросам с UI.
 * В качестве principal и details - {@link JwtUserData}
 */
public class JwtAuthentication extends AbstractAuthenticationToken {

    public JwtAuthentication(JwtUserData jwtUserData) {
        super(null);
        this.setDetails(jwtUserData);
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return getDetails();
    }

}
