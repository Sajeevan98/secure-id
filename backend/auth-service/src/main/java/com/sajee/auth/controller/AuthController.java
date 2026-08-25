package com.sajee.auth.controller;

import com.sajee.auth.common.api.ApiEndpoints;
import com.sajee.auth.common.api.ApiResponse;
import com.sajee.auth.dto.request.LoginRequest;
import com.sajee.auth.dto.request.LogoutRequest;
import com.sajee.auth.dto.request.RegisterRequest;
import com.sajee.auth.dto.request.ResendVerificationRequest;
import com.sajee.auth.dto.response.LoginResponse;
import com.sajee.auth.dto.response.RefreshTokenResponse;
import com.sajee.auth.dto.request.RefreshTokenRequest;
import com.sajee.auth.dto.response.RegisterResponse;
import com.sajee.auth.security.email.EmailVerificationService;
import com.sajee.auth.security.refresh.RefreshTokenService;
import com.sajee.auth.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiEndpoints.AUTH)
@RequiredArgsConstructor
public class AuthController {

    private final AccountService accountService;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/registration")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RegisterResponse> registration(@Valid @RequestBody RegisterRequest request) {

        RegisterResponse response = accountService.register(request);
        return ApiResponse.success(response);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {

        LoginResponse response = accountService.login(
                request,
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent")
        );

        return ApiResponse.success(response);
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<RefreshTokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {

        RefreshTokenResponse response = refreshTokenService.rotate(
                request.refreshToken(), httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent")
        );

        return ApiResponse.success(response);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @Valid @RequestBody LogoutRequest request
    ) {
        refreshTokenService.logout(request.refreshToken());
    }

    @GetMapping("/verify-email")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<String> verifyEmail(@RequestParam String token) {

        emailVerificationService.verify(token);

        return ApiResponse.success(
                "Email verified successfully."
        );
    }

    @PostMapping("/resend-verification")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<String> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request) {

        String token = accountService.resendVerification(request);

        return ApiResponse.success(token);
    }
}
