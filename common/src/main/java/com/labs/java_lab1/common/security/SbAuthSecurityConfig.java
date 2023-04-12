package com.labs.java_lab1.common.security;

import com.labs.java_lab1.common.security.props.SecurityProps;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Arrays;
import java.util.Objects;

/**
 * Конфигурация security проекта
 */
@Slf4j
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
class SbAuthSecurityConfig {

    private final SecurityProps securityProps;

    /**
     * Шифровальщик паролей
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Настройка для JWT
     */
    @SneakyThrows
    @Bean
    public SecurityFilterChain filterChainJwt(HttpSecurity http) {
        http = http.addFilterBefore(
                        new JwtTokenFilter(securityProps.getJwtToken().getSecret()),
                        UsernamePasswordAuthenticationFilter.class
                )
                .requestMatcher(filterPredicate(
                        securityProps.getJwtToken().getRootPath(),
                        securityProps.getJwtToken().getPermitAll()
                ))
        ;
        return finalize(http);
    }

    /**
     * Настройка для интеграции между сервисами
     */
    @SneakyThrows
    @Bean
    public SecurityFilterChain filterChainIntegration(HttpSecurity http) {
        http = http.requestMatcher(filterPredicate(securityProps.getIntegrations().getRootPath()))
                .addFilterBefore(
                        new IntegrationFilter(securityProps.getIntegrations().getApiKey()),
                        UsernamePasswordAuthenticationFilter.class
                );
        return finalize(http);
    }

    @SneakyThrows
    private SecurityFilterChain finalize(HttpSecurity http) {
        return http.csrf()
                .disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .build();
    }

    private RequestMatcher filterPredicate(String rootPath, String... ignore) {
        // Условие, что
        // 1. Путь сервлета задан
        // 2. Путь сервлета соответствует требуемому паттерну (начинается с rootPath)
        // 3. Путь сервлета не соответствует паттерну игнорируемых путей
        return rq -> Objects.nonNull(rq.getServletPath())
                && rq.getServletPath().startsWith(rootPath)
                && Arrays.stream(ignore).noneMatch(item ->rq.getServletPath().startsWith(item));
    }

}