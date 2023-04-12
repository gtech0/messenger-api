package com.labs.java_lab1.common.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Фильтр аутентификации по JWT.
 * Если запрос попадает в этот фильтр, то запрос обязан содержать корректный JWT токен, иначе фильтр вернёт 401 статус
 */
@RequiredArgsConstructor
class JwtTokenFilter extends OncePerRequestFilter {

    private final String secretKey;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String jwt = request.getHeader(SecurityConst.HEADER_JWT);
        if (jwt == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        // парсинг токена
        JwtUserData userData;
        try {
            var key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            var data = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(jwt.substring(7));
            var idStr = String.valueOf(data.getBody().get("id"));
            userData = new JwtUserData(
                    idStr == null ? null : UUID.fromString(idStr),
                    String.valueOf(data.getBody().get("login")),
                    String.valueOf(data.getBody().get("name"))
            );
        } catch (JwtException e) {
            // может случиться, если токен протух или некорректен
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        var authentication = new JwtAuthentication(userData);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
