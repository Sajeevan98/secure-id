package com.sajee.auth.service;

import com.sajee.auth.common.exception.AuthenticationException;
import com.sajee.auth.common.exception.ConflictException;
import com.sajee.auth.dto.request.LoginRequest;
import com.sajee.auth.dto.request.RegisterRequest;
import com.sajee.auth.dto.response.LoginResponse;
import com.sajee.auth.dto.response.RegisterResponse;
import com.sajee.auth.entity.Account;
import com.sajee.auth.repository.AccountRepository;
import com.sajee.auth.security.jwt.JwtToken;
import com.sajee.auth.security.jwt.JwtTokenService;
import com.sajee.auth.security.login.LoginAttemptService;
import com.sajee.auth.security.login.PasswordService;
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

        return RegisterResponse.from(registerAccount);
    }

    @Override
    public LoginResponse login(LoginRequest request) {

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

        return LoginResponse.from(accessToken);
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
