package com.sajee.auth.account.jwt;

import com.sajee.auth.entity.Account;
import com.sajee.auth.security.jwt.JwtProperties;
import com.sajee.auth.security.jwt.JwtToken;
import com.sajee.auth.security.jwt.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JwtTokenValidationTest {

    private JwtTokenService tokenService;
    private JwtDecoder jwtDecoder;

    private static final String ISSUER = "https://auth.secureid.local";
    private static final String AUDIENCE = "secureid-api";

    @BeforeEach
    void setUp() throws Exception {

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

        generator.initialize(2048);

        KeyPair keyPair = generator.generateKeyPair();

        JwtEncoder encoder = NimbusJwtEncoder.withKeyPair(
                (RSAPublicKey) keyPair.getPublic(),
                (RSAPrivateKey) keyPair.getPrivate()
        ).build();

        JwtProperties properties = new JwtProperties(ISSUER, AUDIENCE, Duration.ofMinutes(15));

        tokenService = new JwtTokenService(encoder, properties);

        jwtDecoder = NimbusJwtDecoder.withPublicKey(
                (RSAPublicKey) keyPair.getPublic()
        ).build();
    }

    @Test
    void shouldDecodeValidToken() {

        Account account = Account.builder()
                .username("sajeevan")
                .email("sajeevan@example.com")
                .passwordHash("hashed-password")
                .build();

        JwtToken token = tokenService.generateAccessToken(account);

        Jwt jwt = jwtDecoder.decode(token.value());

        assertThat(jwt)
                .isNotNull();
        assertThat(jwt.getSubject())
                .isEqualTo(account.getUuid().toString());
        assertThat(jwt.getClaimAsString("username"))
                .isEqualTo("sajeevan");
        assertThat(jwt.getId())
                .isNotBlank();
    }

    @Test
    void shouldRejectTamperedToken() {

        Account account = Account.builder()
                .username("sajeevan")
                .email("sajeevan@example.com")
                .passwordHash("hashed-password")
                .build();

        JwtToken token = tokenService.generateAccessToken(account);

        String[] parts = token.value().split("\\.");

        String tamperedToken = parts[0]
                + "."
                + parts[1]
                + "tampered"
                + "."
                + parts[2];

        assertThatThrownBy(() -> jwtDecoder.decode(tamperedToken))
                .isInstanceOf(JwtException.class);
    }
}
