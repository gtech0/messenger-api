package com.labs.java_lab1.common.security.props;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Java-модель конфига по пути app.security в application.yml
 */
@ConfigurationProperties("app.security")
@Getter
@Setter
@ToString
public class SecurityProps {

    private SecurityJwtTokenProps jwtToken;

    private SecurityIntegrationsProps integrations;

}
