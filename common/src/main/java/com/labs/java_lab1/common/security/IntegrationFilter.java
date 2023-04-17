package com.labs.java_lab1.common.security;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * Фильтр проверки ключа аутентификации API KEY в интеграционных запросах
 */
@RequiredArgsConstructor
class IntegrationFilter extends OncePerRequestFilter {

    private final String apiKey;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (!Objects.equals(request.getHeader(SecurityConst.HEADER_API_KEY), apiKey)) {
            // здесь должна быть проверка токена, но в примере мы просто проверяем наличие хидера
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(new IntegrationAuthentication());
        filterChain.doFilter(request, response);
    }
}
