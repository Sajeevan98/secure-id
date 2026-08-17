package com.sajee.auth.dto.response;

import com.sajee.auth.security.jwt.JwtToken;
import com.sajee.auth.security.refresh.GenerateRefreshTokenResponse;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {

    public static LoginResponse from(JwtToken accessToken, GenerateRefreshTokenResponse refreshToken) {

        return new LoginResponse(
                accessToken.value(),
                refreshToken.token(),
                "Bearer",
                accessToken.expiresInSeconds()
        );
    }
}
