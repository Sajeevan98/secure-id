package com.sajee.auth.account.jwt;

import com.sajee.auth.entity.Account;
import com.sajee.auth.security.jwt.JwtProperties;
import com.sajee.auth.security.jwt.JwtToken;
import com.sajee.auth.security.jwt.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() throws Exception {

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

        generator.initialize(2048);

        KeyPair keyPair = generator.generateKeyPair();

        JwtEncoder encoder = NimbusJwtEncoder.withKeyPair(
                (RSAPublicKey) keyPair.getPublic(),
                (RSAPrivateKey) keyPair.getPrivate()
        ).build();

        JwtProperties properties = new JwtProperties(
                "https://auth.secureid.local",
                "secureid-api",
                Duration.ofMinutes(15),
                Duration.ofDays(30)
        );
        jwtTokenService = new JwtTokenService(encoder, properties);
    }

    // Test JWT generation
    @Test
    void shouldGenerateAccessToken() {

        Account account = Account.builder()
                .username("bob")
                .email("bob@example.com")
                .passwordHash("hashed-password")
                .build();

        JwtToken token = jwtTokenService.generateAccessToken(account);

        assertThat(token)
                .isNotNull();
        assertThat(token.value())
                .isNotBlank();
        assertThat(token.issuedAt())
                .isNotNull();
        assertThat(token.expiresAt())
                .isAfter(token.issuedAt());
        assertThat(token.expiresInSeconds())
                .isEqualTo(900);
    }

    // Test the actual JWT claims
    @Test
    void shouldGenerateTokenWithExpectedClaims() throws Exception {

        Account account = Account.builder()
                .username("bob")
                .email("bob@example.com")
                .passwordHash("hashed-password")
                .build();

        JwtToken token = jwtTokenService.generateAccessToken(account);

        String[] parts = token.value().split("\\.");

        assertThat(parts)
                .hasSize(3);
    }
}
