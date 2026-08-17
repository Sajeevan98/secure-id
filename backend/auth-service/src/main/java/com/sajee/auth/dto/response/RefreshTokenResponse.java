package com.sajee.auth.dto.response;

import java.time.Instant;

public record RefreshTokenResponse(
        String accessToken,
        String tokenType,
        Instant expiresIn
) {
}
