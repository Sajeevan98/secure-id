package com.sajee.auth.security.password;

import com.sajee.auth.common.exception.AuthenticationException;
import com.sajee.auth.entity.Account;
import com.sajee.auth.entity.PasswordResetToken;
import com.sajee.auth.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetTokenGenerator tokenGenerator;
    private final PasswordResetProperties properties;

    public String create(Account account) {

        // Remove previous reset tokens for this account.
        passwordResetTokenRepository.deleteAllByAccount(account);

        String rawToken = tokenGenerator.generateRawToken();
        String tokenHash = tokenGenerator.hash(rawToken);

        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .account(account)
                .tokenHash(tokenHash)
                .expiresAt(
                        Instant.now().plus(properties.tokenTtl())
                )
                .build();

        passwordResetTokenRepository.save(passwordResetToken);

        return rawToken;
    }

    @Transactional(readOnly = true)
    public PasswordResetToken validate(String rawToken) {

        String tokenHash = tokenGenerator.hash(rawToken);

        PasswordResetToken token = passwordResetTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() ->
                        new AuthenticationException(
                                "AUTH_INVALID_PASSWORD_RESET_TOKEN",
                                "Invalid password reset token."
                        )
                );

        if (token.isUsed()) {
            throw new AuthenticationException(
                    "AUTH_PASSWORD_RESET_TOKEN_USED",
                    "Password reset token has already been used."
            );
        }

        if (token.isExpired()) {
            throw new AuthenticationException(
                    "AUTH_PASSWORD_RESET_TOKEN_EXPIRED",
                    "Password reset token has expired."
            );
        }
        return token;
    }

    public void markUsed(PasswordResetToken token) {
        token.markUsed();
    }
}
