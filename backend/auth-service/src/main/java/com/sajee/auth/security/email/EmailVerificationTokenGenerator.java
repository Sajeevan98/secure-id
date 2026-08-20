package com.sajee.auth.security.email;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class EmailVerificationTokenGenerator {

    private static final int TOKEN_BYTES = 32;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {

        byte[] token = new byte[TOKEN_BYTES];

        secureRandom.nextBytes(token);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(token);
    }
}
