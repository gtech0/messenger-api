package com.labs.java_lab1.common.security.props;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Свойства security для аутентификации JWT
 */
@Getter
@Setter
@ToString
public class SecurityJwtTokenProps {

    private String[] permitAll;

    private String secret;

    private Long expiration;

    private String rootPath;

}
