package com.sajee.auth.security.password;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "secureid.password-reset")
public record PasswordResetProperties(
        Duration tokenTtl
) {
}
