package com.labs.java_lab1.common.security.props;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Настройки security для интеграционного взаимодействия
 */
@Getter
@Setter
@ToString
public class SecurityIntegrationsProps {

    private String apiKey;

    private String rootPath;

}
