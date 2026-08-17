package com.sajee.auth.dto.response;

import java.time.Instant;

public record RefreshTokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Instant expiresIn
) {
}
