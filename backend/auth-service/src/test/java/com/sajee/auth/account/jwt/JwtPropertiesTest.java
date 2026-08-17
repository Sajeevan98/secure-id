package com.sajee.auth.account.jwt;

import com.sajee.auth.security.jwt.JwtProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtPropertiesTest {

    @Test
    void shouldCreateJwtProperties() {

        JwtProperties properties = new JwtProperties(
                "https://auth.secureid.local",
                "secureid-api",
                Duration.ofMinutes(15),
                Duration.ofDays(30)
        );

        assertThat(properties.issuer())
                .isEqualTo("https://auth.secureid.local");
        assertThat(properties.audience())
                .isEqualTo("secureid-api");
        assertThat(properties.accessTokenTtl())
                .isEqualTo(Duration.ofMinutes(15));
    }
}
