package com.sajee.auth.dto.response;

import com.sajee.auth.security.jwt.JwtToken;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {

    public static LoginResponse from(JwtToken token) {

        return new LoginResponse(
                token.value(),
                "Bearer",
                token.expiresInSeconds()
        );
    }
}
