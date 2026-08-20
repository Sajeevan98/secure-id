package com.sajee.auth.email;

import com.sajee.auth.security.email.EmailVerificationTokenHasher;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EmailVerificationTokenHasherTest {

    private final EmailVerificationTokenHasher hasher =
            new EmailVerificationTokenHasher();

    @Test
    void shouldGenerateConsistentHash() {

        String hash1 = hasher.hash("test-token");
        String hash2 = hasher.hash("test-token");

        assertThat(hash1)
                .isEqualTo(hash2)
                .hasSize(64);
    }

    @Test
    void shouldGenerateDifferentHashForDifferentTokens() {

        String hash1 = hasher.hash("token-1");
        String hash2 = hasher.hash("token-2");

        assertThat(hash1)
                .isNotEqualTo(hash2);
    }
}
