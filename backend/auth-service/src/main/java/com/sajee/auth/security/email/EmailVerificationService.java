package com.sajee.auth.security.email;

import com.sajee.auth.common.exception.AuthenticationException;
import com.sajee.auth.entity.Account;
import com.sajee.auth.entity.EmailVerificationToken;
import com.sajee.auth.repository.EmailVerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EmailVerificationService {

    private final EmailVerificationProperties properties;

    private final EmailVerificationTokenGenerator tokenGenerator;
    private final EmailVerificationTokenHasher tokenHasher;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;


    public String create(Account account) {

        String rawToken = tokenGenerator.generate();

        String tokenHash = tokenHasher.hash(rawToken);

        Instant expiresAt = Instant.now()
                .plus(properties.tokenTtl());

        EmailVerificationToken token = EmailVerificationToken.builder()
                .account(account)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .build();

        emailVerificationTokenRepository.save(token);

        log.debug("Email verification token created for account {}", account.getUuid());

        return rawToken;
    }


    public void verify(String rawToken) {

        String tokenHash = tokenHasher.hash(rawToken);

        log.debug(
                "Email verification token hash: {}",
                tokenHash
        );

        EmailVerificationToken token = emailVerificationTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new AuthenticationException(
                        "AUTH_INVALID_EMAIL_VERIFICATION_TOKEN", "Invalid email verification token."
                ));

        if (token.isVerified()) {
            throw new AuthenticationException(
                    "AUTH_EMAIL_VERIFICATION_TOKEN_USED", "Email verification token has already been used."
            );
        }

        if (token.isExpired()) {
            throw new AuthenticationException(
                    "AUTH_EMAIL_VERIFICATION_TOKEN_EXPIRED", "Email verification token has expired."
            );
        }

        Account account = token.getAccount();

        if (account.isEmailVerified()) {
            throw new AuthenticationException(
                    "AUTH_EMAIL_ALREADY_VERIFIED", "Email address is already verified."
            );
        }

        account.verifyEmail();

        token.markVerified();

        log.info("Email verified successfully for account {}", account.getUuid()
        );
    }
}
