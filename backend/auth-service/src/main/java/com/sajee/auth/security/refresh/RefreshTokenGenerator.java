package com.sajee.auth.security.refresh;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class RefreshTokenGenerator {

    private static final int TOKEN_BYTE_LENGTH = 64;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {

        byte[] tokenBytes = new byte[TOKEN_BYTE_LENGTH];

        secureRandom.nextBytes(tokenBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);
    }
}