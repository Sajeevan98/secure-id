package com.sajee.auth.email;

import com.sajee.auth.entity.Account;
import com.sajee.auth.entity.EmailVerificationToken;
import com.sajee.auth.repository.EmailVerificationTokenRepository;
import com.sajee.auth.security.email.EmailVerificationProperties;
import com.sajee.auth.security.email.EmailVerificationService;
import com.sajee.auth.security.email.EmailVerificationTokenGenerator;
import com.sajee.auth.security.email.EmailVerificationTokenHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailVerificationServiceTest {

    @Mock
    private EmailVerificationTokenGenerator tokenGenerator;

    @Mock
    private EmailVerificationTokenHasher tokenHasher;

    @Mock
    private EmailVerificationTokenRepository tokenRepository;

    @Mock
    private EmailVerificationProperties properties;

    private EmailVerificationService service;


    @BeforeEach
    void setUp() {

        service = new EmailVerificationService(
                properties,
                tokenGenerator,
                tokenHasher,
                tokenRepository
        );
    }


    @Test
    void shouldCreateEmailVerificationToken() {

        Account account = mock(Account.class);

        when(account.getUuid())
                .thenReturn(UUID.randomUUID());

        when(tokenGenerator.generate())
                .thenReturn("raw-token");

        when(tokenHasher.hash("raw-token"))
                .thenReturn("hashed-token");

        when(properties.tokenTtl())
                .thenReturn(Duration.ofMinutes(15));

        EmailVerificationToken savedToken =
                mock(EmailVerificationToken.class);

        when(tokenRepository.save(any(EmailVerificationToken.class)))
                .thenReturn(savedToken);

        String result = service.create(account);

        assertThat(result)
                .isEqualTo("raw-token");

        verify(tokenGenerator)
                .generate();

        verify(tokenHasher)
                .hash("raw-token");

        verify(tokenRepository)
                .save(any(EmailVerificationToken.class));
    }


    @Test
    void shouldVerifyEmailSuccessfully() {

        Account account = mock(Account.class);

        when(account.isEmailVerified())
                .thenReturn(false);

        EmailVerificationToken token =
                mock(EmailVerificationToken.class);

        when(token.isVerified())
                .thenReturn(false);

        when(token.isExpired())
                .thenReturn(false);

        when(token.getAccount())
                .thenReturn(account);

        when(tokenHasher.hash("raw-token"))
                .thenReturn("hashed-token");

        when(tokenRepository.findByTokenHash("hashed-token"))
                .thenReturn(Optional.of(token));

        service.verify("raw-token");

        verify(account)
                .verifyEmail();

        verify(token)
                .markVerified();
    }


    @Test
    void shouldRejectInvalidToken() {

        when(tokenHasher.hash("invalid-token"))
                .thenReturn("hashed-token");

        when(tokenRepository.findByTokenHash("hashed-token"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.verify("invalid-token")
        )
                .isInstanceOf(com.sajee.auth.common.exception.AuthenticationException.class)
                .hasMessage("Invalid email verification token.");
    }


    @Test
    void shouldRejectExpiredToken() {

        EmailVerificationToken token =
                mock(EmailVerificationToken.class);

        when(token.isVerified())
                .thenReturn(false);

        when(token.isExpired())
                .thenReturn(true);

        when(tokenHasher.hash("raw-token"))
                .thenReturn("hashed-token");

        when(tokenRepository.findByTokenHash("hashed-token"))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() ->
                service.verify("raw-token")
        )
                .isInstanceOf(com.sajee.auth.common.exception.AuthenticationException.class)
                .hasMessage("Email verification token has expired.");

        verify(token, never())
                .markVerified();
    }


    @Test
    void shouldRejectAlreadyUsedToken() {

        EmailVerificationToken token =
                mock(EmailVerificationToken.class);

        when(token.isVerified())
                .thenReturn(true);

        when(tokenHasher.hash("raw-token"))
                .thenReturn("hashed-token");

        when(tokenRepository.findByTokenHash("hashed-token"))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() ->
                service.verify("raw-token")
        )
                .isInstanceOf(com.sajee.auth.common.exception.AuthenticationException.class)
                .hasMessage("Email verification token has already been used.");

        verify(token, never())
                .markVerified();
    }


    @Test
    void shouldRejectAlreadyVerifiedAccount() {

        Account account = mock(Account.class);

        when(account.isEmailVerified())
                .thenReturn(true);

        EmailVerificationToken token =
                mock(EmailVerificationToken.class);

        when(token.isVerified())
                .thenReturn(false);

        when(token.isExpired())
                .thenReturn(false);

        when(token.getAccount())
                .thenReturn(account);

        when(tokenHasher.hash("raw-token"))
                .thenReturn("hashed-token");

        when(tokenRepository.findByTokenHash("hashed-token"))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() ->
                service.verify("raw-token")
        )
                .isInstanceOf(com.sajee.auth.common.exception.AuthenticationException.class)
                .hasMessage("Email address is already verified.");

        verify(account, never())
                .verifyEmail();

        verify(token, never())
                .markVerified();
    }

}
