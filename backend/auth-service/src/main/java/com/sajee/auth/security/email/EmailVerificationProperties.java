package com.sajee.auth.security.email;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "secureid.email.verification")
public record EmailVerificationProperties(
        Duration tokenTtl
) {
}
