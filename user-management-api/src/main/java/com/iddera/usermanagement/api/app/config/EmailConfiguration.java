package com.iddera.usermanagement.api.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "iddera.email")
public class EmailConfiguration {
    private String userActivationUrl;
    private String userForgotPasswordUrl;
}
