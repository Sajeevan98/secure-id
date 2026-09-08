package com.sajee.auth.service;

import com.sajee.auth.common.exception.AuthenticationException;
import com.sajee.auth.common.exception.ConflictException;
import com.sajee.auth.dto.request.*;
import com.sajee.auth.dto.response.LoginResponse;
import com.sajee.auth.dto.response.RegisterResponse;
import com.sajee.auth.entity.Account;
import com.sajee.auth.entity.PasswordResetToken;
import com.sajee.auth.repository.AccountRepository;
import com.sajee.auth.repository.EmailVerificationTokenRepository;
import com.sajee.auth.security.email.EmailSender;
import com.sajee.auth.security.email.EmailVerificationService;
import com.sajee.auth.security.jwt.JwtToken;
import com.sajee.auth.security.jwt.JwtTokenService;
import com.sajee.auth.security.login.LoginAttemptService;
import com.sajee.auth.security.login.PasswordService;
import com.sajee.auth.security.password.PasswordResetService;
import com.sajee.auth.security.refresh.GenerateRefreshTokenResponse;
import com.sajee.auth.security.refresh.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordService passwordService;
    private final LoginAttemptService loginAttemptService;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationService emailVerificationService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetService passwordResetService;
    private final EmailSender emailSender;

    @Override
    public RegisterResponse register(RegisterRequest request) {

        String username = request.username().trim().toLowerCase(Locale.ROOT);
        String email = normalizeEmail(request.email());
        String passwordHash = passwordEncoder.encode(request.password());

        if (accountRepository.existsByUsername(username)) {

            log.debug("{} username already exists", username);
            throw new ConflictException("AUTH_USERNAME_ALREADY_EXISTS", "An account with this username already exists");
        }

        if (accountRepository.existsByEmail(email)) {

            log.debug("{} email already exists", email);
            throw new ConflictException("AUTH_EMAIL_ALREADY_EXISTS", "An account with this email already exists");
        }

        Account account = Account.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordHash)
                .build();

        Account registerAccount = accountRepository.save(account);
        log.debug("Registration successful for {}", request.email());

        String verificationToken = emailVerificationService.create(registerAccount);
        log.debug("Email verification token for {} {}:", request.email(), verificationToken);

        // send verify email
        emailSender.sendVerificationEmail(registerAccount.getEmail(), verificationToken);

        return RegisterResponse.from(registerAccount, verificationToken);
    }

    @Override
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {

        String email = normalizeEmail(request.email());

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(this::invalidCredentials);

        account.unlockIfLockExpired();

        if (account.isDisabled()) {
            log.debug("{} account is disabled.", email);
            throw new AuthenticationException("AUTH_ACCOUNT_DISABLED", "Account is disabled.");
        }

        if (account.isLocked()) {
            log.debug("{} account is temporarily locked.", email);
            throw new AuthenticationException("AUTH_ACCOUNT_LOCKED", "Account is temporarily locked.");
        }

        if (!account.isEmailVerified()) {
            log.debug("{} email is not verified.", email);
            throw new AuthenticationException("AUTH_EMAIL_NOT_VERIFIED", "Email address is not verified.");
        }

        boolean passwordMatches = passwordService.matches(
                request.password(),
                account.getPasswordHash()
        );

        if (!passwordMatches) {
            loginAttemptService.recordFailedAttempt(account.getUuid());
            throw invalidCredentials();
        }

        loginAttemptService.recordSuccessfulLogin(account.getUuid());

        JwtToken accessToken = jwtTokenService.generateAccessToken(account);

        GenerateRefreshTokenResponse refreshToken = refreshTokenService
                .create(account, ipAddress, userAgent);

        return LoginResponse.from(accessToken, refreshToken);
    }

    @Override
    public void resendVerification(ResendVerificationRequest request) {

        Account account = accountRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthenticationException(
                        "AUTH_ACCOUNT_NOT_FOUND",
                        "Account not found."
                ));

        if (account.isEmailVerified()) {
            throw new AuthenticationException(
                    "AUTH_EMAIL_ALREADY_VERIFIED",
                    "Email is already verified."
            );
        }

        emailVerificationTokenRepository.deleteAllByAccount(account);

        String verificationToken = emailVerificationService.create(account);

        emailSender.sendVerificationEmail(
                account.getEmail(),
                verificationToken
        );
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {

        Account account = accountRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthenticationException(
                        "AUTH_ACCOUNT_NOT_FOUND", "Account not found."
                ));

        String resetToken = passwordResetService.create(account);

        log.debug("Password reset token created for account {}", account.getUuid());

        emailSender.sendPasswordResetEmail(account.getEmail(), resetToken);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {

        PasswordResetToken token = passwordResetService.validate(request.token());

        Account account = token.getAccount();

        String passwordHash = passwordEncoder.encode(request.newPassword());

        account.changePassword(passwordHash);

        passwordResetService.markUsed(token);

        log.debug("Password reset successfully for account {}", account.getUuid());
    }

    @Override
    public void validatePasswordResetToken(String token) {

        passwordResetService.validate(token);
    }

    // ========== Helper Methods ==========
    private String normalizeEmail(String email) {

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private AuthenticationException invalidCredentials() {

        return new AuthenticationException(
                "AUTH_INVALID_CREDENTIALS",
                "Invalid email or password."
        );
    }
}
