package com.sajee.auth.account.jwt;

import com.sajee.auth.security.jwt.JwtKeyConfiguration;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtKeyConfigurationTest {

    private final JwtKeyConfiguration configuration = new JwtKeyConfiguration();

    @Test
    void shouldGenerateRsaKeyPair() {

        KeyPair keyPair = configuration.jwtKeyPair();

        assertThat(keyPair).isNotNull();
        assertThat(keyPair.getPrivate()).isInstanceOf(RSAPrivateKey.class);
        assertThat(keyPair.getPublic()).isInstanceOf(RSAPublicKey.class);
    }

    @Test
    void shouldGenerate2048BitRsaKey() {

        KeyPair keyPair = configuration.jwtKeyPair();

        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

        assertThat(publicKey.getModulus().bitLength())
                .isEqualTo(2048);
    }
}
