package com.sajee.auth.dto.response;

import com.sajee.auth.entity.RefreshToken;

import java.time.Instant;
import java.util.UUID;

public record SessionResponse(
        UUID uuid,
        String createdIp,
        String userAgent,
        Instant createdAt,
        Instant expiresAt
) {

    public static SessionResponse from(RefreshToken token) {

        return new SessionResponse(
                token.getUuid(),
                token.getCreatedIp(),
                token.getUserAgent(),
                token.getCreatedAt(),
                token.getExpiresAt()
        );
    }
}
