package com.sajee.auth.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "secureid.jwt")
public record JwtProperties(

        String issuer,
        String audience,
        Duration accessTokenTtl,
        Duration refreshTokenTtl
) {
}
