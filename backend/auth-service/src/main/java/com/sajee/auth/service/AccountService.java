package com.sajee.auth.service;

import com.sajee.auth.dto.request.LoginRequest;
import com.sajee.auth.dto.request.RegisterRequest;
import com.sajee.auth.dto.request.ResendVerificationRequest;
import com.sajee.auth.dto.response.LoginResponse;
import com.sajee.auth.dto.response.RegisterResponse;

public interface AccountService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request, String ipAddress, String userAgent);

    String resendVerification(ResendVerificationRequest request);
}
