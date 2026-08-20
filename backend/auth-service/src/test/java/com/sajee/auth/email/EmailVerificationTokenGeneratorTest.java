package com.sajee.auth.email;

import com.sajee.auth.security.email.EmailVerificationTokenGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EmailVerificationTokenGeneratorTest {

    private final EmailVerificationTokenGenerator generator =
            new EmailVerificationTokenGenerator();

    @Test
    void shouldGenerateToken() {

        String token = generator.generate();

        assertThat(token)
                .isNotBlank()
                .doesNotContain(" ");
    }

    @Test
    void shouldGenerateDifferentTokens() {

        String token1 = generator.generate();
        String token2 = generator.generate();

        assertThat(token1).isNotEqualTo(token2);
    }
}
