package com.sajee.auth.refresh;

import com.sajee.auth.security.refresh.RefreshTokenHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RefreshTokenHasherTest {

    private RefreshTokenHasher tokenHasher;

    @BeforeEach
    void setUp() {
        tokenHasher = new RefreshTokenHasher();
    }

    @Test
    void shouldProduceDeterministicSha256Hash() {

        String token = "test-refresh-token";

        String first = tokenHasher.hash(token);
        String second = tokenHasher.hash(token);

        assertThat(first)
                .isEqualTo(second);
    }

    @Test
    void shouldProduce64CharacterHash() {

        String hash = tokenHasher.hash("test-refresh-token");

        assertThat(hash)
                .hasSize(64);
    }

    @Test
    void differentTokensShouldProduceDifferentHashes() {

        String first = tokenHasher.hash("token-one");

        String second = tokenHasher.hash("token-two");

        assertThat(first)
                .isNotEqualTo(second);
    }
}
