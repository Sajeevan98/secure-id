package com.sajee.auth.refresh;

import com.sajee.auth.entity.Account;
import com.sajee.auth.entity.RefreshToken;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class RefreshTokenTest {

    Account account = Account.builder()
            .username("sajeevan")
            .email("sajeevan@example.com")
            .passwordHash("hash-password")
            .build();

    // Active
    @Test
    void shouldIdentifyActiveToken() {

        RefreshToken token = RefreshToken.builder()
                .account(account)
                .tokenHash("hash")
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        assertThat(token.isExpired()).isFalse();
        assertThat(token.isRevoked()).isFalse();
        assertThat(token.isActive()).isTrue();
    }

    // Expired
    @Test
    void shouldIdentifyExpiredToken() {

        RefreshToken token = RefreshToken.builder()
                .account(account)
                .tokenHash("hash")
                .expiresAt(Instant.now().minusSeconds(1))
                .build();

        assertThat(token.isExpired()).isTrue();
        assertThat(token.isActive()).isFalse();
    }

    // Revoked
    @Test
    void shouldIdentifyRevokedToken() {

        RefreshToken token = RefreshToken.builder()
                .account(account)
                .tokenHash("hash")
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        token.revoke();

        assertThat(token.isRevoked()).isTrue();
        assertThat(token.isActive()).isFalse();
        assertThat(token.getRevokedAt()).isNotNull();
    }
}
