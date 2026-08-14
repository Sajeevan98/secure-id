package com.sajee.auth.security.jwt;

import java.time.Instant;

public record JwtToken(
        String value,
        Instant issuedAt,
        Instant expiresAt
) {

    public long expiresInSeconds() {
        return expiresAt.getEpochSecond() - issuedAt.getEpochSecond();
    }
}