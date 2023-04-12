package com.labs.java_lab1.user.service;

import com.labs.java_lab1.user.dto.AuthDto;
import com.labs.java_lab1.common.exception.UserNotFoundException;
import com.labs.java_lab1.user.repository.UserRepository;
import com.labs.java_lab1.common.security.props.SecurityProps;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static java.lang.System.currentTimeMillis;

/**
 * Сервисный бин аутентификации
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;

    private final SecurityProps securityProps;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * Генерация токена
     *
     * @param authDto логин и пароль (данные входа в систему)
     * @return строка с jwt
     */
    public String generateToken(AuthDto authDto) {
        var user = userRepository.findByLogin(authDto.getLogin());

        if (user.isEmpty()) {
            throw new UserNotFoundException("No user");
        }

        if (!bCryptPasswordEncoder.matches(authDto.getPassword(), user.get().getPassword())) {
            throw new UserNotFoundException("Incorrect password");
        }
        var key = Keys.hmacShaKeyFor(securityProps.getJwtToken().getSecret().getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject(user.get().getFullName())
                .setClaims(Map.of(
                        "login", user.get().getLogin(),
                        "id", user.get().getUuid(),
                        "name", user.get().getFullName()
                ))
                .setId(UUID.randomUUID().toString())
                .setExpiration(new Date(currentTimeMillis() + securityProps.getJwtToken().getExpiration()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

}
