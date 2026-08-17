package com.sajee.auth.refresh;

import com.sajee.auth.security.refresh.RefreshTokenGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RefreshTokenGeneratorTest {

    private RefreshTokenGenerator refreshTokenGenerator;

    @BeforeEach
    void setUp() {
        refreshTokenGenerator = new RefreshTokenGenerator();
    }

    @Test
    void shouldGenerateDifferentTokens() {

        String first = refreshTokenGenerator.generate();
        String second = refreshTokenGenerator.generate();

        assertThat(first)
                .isNotEqualTo(second);
    }

    @Test
    void shouldGenerateUrlSafeToken() {

        String token = refreshTokenGenerator.generate();

        assertThat(token)
                .isNotBlank();
        assertThat(token)
                .doesNotContain("+");
        assertThat(token)
                .doesNotContain("/");
        assertThat(token)
                .doesNotContain("=");
    }
}
