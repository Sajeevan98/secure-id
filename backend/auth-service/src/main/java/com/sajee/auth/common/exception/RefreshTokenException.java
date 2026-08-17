package com.sajee.auth.common.exception;

public class RefreshTokenException extends AuthenticationException {

    public RefreshTokenException(String code, String message) {
        super(code, message);
    }
}
