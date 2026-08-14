package com.sajee.auth.account.jwt;

import com.sajee.auth.security.jwt.JwtKeyConfiguration;
import com.sajee.auth.security.jwt.JwtKeyProvider;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtKeyProviderTest {

    @Test
    void shouldExposeRsaKeys() {

        JwtKeyConfiguration configuration = new JwtKeyConfiguration();

        KeyPair keyPair = configuration.jwtKeyPair();

        JwtKeyProvider provider = new JwtKeyProvider(
                (RSAPrivateKey) keyPair.getPrivate(),
                (RSAPublicKey) keyPair.getPublic()
        );

        assertThat(provider.getPrivateKey())
                .isNotNull();
        assertThat(provider.getPublicKey())
                .isNotNull();
        assertThat(provider.getPrivateKey())
                .isNotEqualTo(provider.getPublicKey());
    }
}
