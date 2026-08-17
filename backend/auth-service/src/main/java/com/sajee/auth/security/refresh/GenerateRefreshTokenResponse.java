package com.sajee.auth.security.refresh;

import java.time.Instant;
import java.util.UUID;

public record GenerateRefreshTokenResponse(
        String token,
        UUID uuid,
        Instant expiresAt
) {
}
