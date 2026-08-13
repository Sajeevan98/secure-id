package com.sajee.auth.security.login;

import java.time.Duration;

public class LoginSecurityPolicy {

    private LoginSecurityPolicy() {

    }

    public static final int MAX_FAILED_ATTEMPTS = 5;

    public static final Duration LOCK_DURATION = Duration.ofMinutes(15);
}
