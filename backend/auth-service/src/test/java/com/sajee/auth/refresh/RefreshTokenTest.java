package com.sajee.auth.refresh;

import com.sajee.auth.entity.RefreshToken;
import com.sajee.auth.enums.RefreshTokenRevocationReason;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class RefreshTokenTest {

    @Test
    void shouldBeActiveWhenNotExpiredAndNotRevoked() {
        RefreshToken token = RefreshToken.builder()
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        assertTrue(token.isActive());
        assertFalse(token.isExpired());
        assertFalse(token.isRevoked());
    }

    @Test
    void shouldBeExpiredWhenExpirationTimeIsInThePast() {
        RefreshToken token = RefreshToken.builder()
                .expiresAt(Instant.now().minusSeconds(60))
                .build();

        assertTrue(token.isExpired());
        assertFalse(token.isActive());
    }

    @Test
    void shouldBeExpiredWhenExpirationTimeIsNow() {
        Instant expiresAt = Instant.now();

        RefreshToken token = RefreshToken.builder()
                .expiresAt(expiresAt)
                .build();

        assertTrue(token.isExpired());
        assertFalse(token.isActive());
    }

    @Test
    void shouldBeRevokedAfterRevoke() {
        RefreshToken token = RefreshToken.builder()
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        token.revoke(RefreshTokenRevocationReason.LOGOUT);

        assertTrue(token.isRevoked());
        assertFalse(token.isActive());
        assertNotNull(token.getRevokedAt());
        assertEquals(
                RefreshTokenRevocationReason.LOGOUT,
                token.getRevocationReason()
        );
    }

    @Test
    void shouldNotChangeRevocationWhenAlreadyRevoked() {
        RefreshToken token = RefreshToken.builder()
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        token.revoke(RefreshTokenRevocationReason.LOGOUT);
        Instant firstRevokedAt = token.getRevokedAt();

        token.revoke(RefreshTokenRevocationReason.ADMIN_REVOKED);

        assertEquals(firstRevokedAt, token.getRevokedAt());
        assertEquals(
                RefreshTokenRevocationReason.LOGOUT,
                token.getRevocationReason()
        );
    }

    @Test
    void shouldMarkTokenAsReplaced() {
        RefreshToken original = RefreshToken.builder()
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        RefreshToken replacement = RefreshToken.builder()
                .expiresAt(Instant.now().plusSeconds(120))
                .build();

        original.markReplacedBy(replacement);

        assertSame(replacement, original.getReplacedByToken());
        assertTrue(original.isRevoked());
        assertFalse(original.isActive());
        assertEquals(
                RefreshTokenRevocationReason.ROTATED,
                original.getRevocationReason()
        );
    }

    @Test
    void shouldGenerateUuidByDefault() {
        RefreshToken token = RefreshToken.builder()
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        assertNotNull(token.getUuid());
    }
}
