package com.sajee.auth.common.exception;

public class AuthenticationException extends BusinessException {

    public AuthenticationException(String code, String message) {
        super(code, message);
    }
}
