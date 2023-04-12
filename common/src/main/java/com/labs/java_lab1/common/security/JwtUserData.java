package com.labs.java_lab1.common.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

/**
 * Данные Principal в Authentication - инфо о текущем пользователе
 */
@Getter
@AllArgsConstructor
public class JwtUserData {

    private final UUID id;

    private final String login;

    private final String name;

}
