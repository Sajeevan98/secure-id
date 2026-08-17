package com.sajee.auth.refresh;

import com.sajee.auth.common.exception.AuthenticationException;
import com.sajee.auth.dto.response.RefreshTokenResponse;
import com.sajee.auth.entity.Account;
import com.sajee.auth.entity.RefreshToken;
import com.sajee.auth.repository.RefreshTokenRepository;
import com.sajee.auth.security.jwt.JwtProperties;
import com.sajee.auth.security.jwt.JwtToken;
import com.sajee.auth.security.jwt.JwtTokenService;
import com.sajee.auth.security.refresh.GenerateRefreshTokenResponse;
import com.sajee.auth.security.refresh.RefreshTokenGenerator;
import com.sajee.auth.security.refresh.RefreshTokenHasher;
import com.sajee.auth.security.refresh.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private RefreshTokenGenerator tokenGenerator;

    @Mock
    private RefreshTokenHasher tokenHasher;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private Account account;

    @Mock
    private RefreshToken refreshToken;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    void create_shouldCreateAndPersistRefreshToken() {

        // Arrange
        String rawToken = "raw-token";
        String tokenHash = "hashed-token";
        String ipAddress = "127.0.0.1";
        String userAgent = "JUnit";

        Duration ttl = Duration.ofDays(30);

        UUID uuid = UUID.randomUUID();
        Instant expiresAt = Instant.now().plus(ttl);

        when(tokenGenerator.generate())
                .thenReturn(rawToken);

        when(tokenHasher.hash(rawToken))
                .thenReturn(tokenHash);

        when(jwtProperties.refreshTokenTtl())
                .thenReturn(ttl);

        RefreshToken savedToken = RefreshToken.builder()
                .uuid(uuid)
                .account(account)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .createdIp(ipAddress)
                .userAgent(userAgent)
                .build();

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenReturn(savedToken);

        // Act
        GenerateRefreshTokenResponse response = refreshTokenService
                .create(account, ipAddress, userAgent);

        // Assert
        assertThat(response.token())
                .isEqualTo(rawToken);

        assertThat(response.uuid())
                .isEqualTo(uuid);

        assertThat(response.expiresAt())
                .isEqualTo(expiresAt);

        ArgumentCaptor<RefreshToken> captor =
                ArgumentCaptor.forClass(RefreshToken.class);

        verify(refreshTokenRepository)
                .save(captor.capture());

        RefreshToken entity = captor.getValue();

        assertThat(entity.getAccount())
                .isSameAs(account);

        assertThat(entity.getTokenHash())
                .isEqualTo(tokenHash);

        assertThat(entity.getCreatedIp())
                .isEqualTo(ipAddress);

        assertThat(entity.getUserAgent())
                .isEqualTo(userAgent);

        verify(tokenGenerator).generate();
        verify(tokenHasher).hash(rawToken);
    }

    @Test
    void refresh_shouldReturnAccessToken_whenTokenIsValid() {

        // Arrange
        String rawToken = "raw-token";
        String tokenHash = "hashed-token";

        Instant accessTokenExpiresAt = Instant.now().plus(Duration.ofMinutes(15));

        JwtToken accessToken = new JwtToken(
                "access-token",
                Instant.now(),
                accessTokenExpiresAt
        );

        when(tokenHasher.hash(rawToken))
                .thenReturn(tokenHash);

        when(refreshTokenRepository.findByTokenHash(tokenHash))
                .thenReturn(Optional.of(refreshToken));

        when(refreshToken.isRevoked())
                .thenReturn(false);

        when(refreshToken.isExpired())
                .thenReturn(false);

        when(refreshToken.getAccount())
                .thenReturn(account);

        when(account.isDisabled())
                .thenReturn(false);

        when(account.isLocked())
                .thenReturn(false);

        when(jwtTokenService.generateAccessToken(account))
                .thenReturn(accessToken);

        // Act
        RefreshTokenResponse response = refreshTokenService.refresh(rawToken);

        // Assert
        assertThat(response.accessToken())
                .isEqualTo("access-token");

        assertThat(response.tokenType())
                .isEqualTo("Bearer");

        assertThat(response.expiresIn())
                .isEqualTo(accessTokenExpiresAt);

        verify(tokenHasher)
                .hash(rawToken);

        verify(refreshTokenRepository)
                .findByTokenHash(tokenHash);

        verify(jwtTokenService)
                .generateAccessToken(account);
    }

    // Invalid refresh token
    @Test
    void refresh_shouldThrow_whenTokenDoesNotExist() {

        // Arrange
        String rawToken = "invalid-token";
        String tokenHash = "hashed-invalid-token";

        when(tokenHasher.hash(rawToken))
                .thenReturn(tokenHash);

        when(refreshTokenRepository.findByTokenHash(tokenHash))
                .thenReturn(Optional.empty());

        // Act
        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> refreshTokenService.refresh(rawToken)
        );

        // Assert
        assertThat(exception.getCode())
                .isEqualTo("AUTH_INVALID_REFRESH_TOKEN");

        assertThat(exception.getMessage())
                .isEqualTo("Invalid refresh token.");

        verify(jwtTokenService, never())
                .generateAccessToken(any());
    }

    //Revoked token
    @Test
    void refresh_shouldThrow_whenTokenIsRevoked() {

        // Arrange
        String rawToken = "raw-token";

        when(tokenHasher.hash(rawToken))
                .thenReturn("hashed-token");

        when(refreshTokenRepository.findByTokenHash("hashed-token"))
                .thenReturn(Optional.of(refreshToken));

        when(refreshToken.isRevoked())
                .thenReturn(true);

        // Act
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> refreshTokenService.refresh(rawToken)
        );

        // Assert
        assertThat(exception.getCode())
                .isEqualTo("AUTH_REFRESH_TOKEN_REVOKED");

        verify(refreshToken, never())
                .isExpired();

        verify(refreshToken, never())
                .getAccount();

        verify(jwtTokenService, never())
                .generateAccessToken(any());
    }

    // Expired token
    @Test
    void refresh_shouldThrow_whenTokenIsExpired() {

        // Arrange
        String rawToken = "raw-token";

        when(tokenHasher.hash(rawToken))
                .thenReturn("hashed-token");

        when(refreshTokenRepository.findByTokenHash("hashed-token"))
                .thenReturn(Optional.of(refreshToken));

        when(refreshToken.isRevoked())
                .thenReturn(false);

        when(refreshToken.isExpired())
                .thenReturn(true);

        // Act
        AuthenticationException exception =
                assertThrows(
                        AuthenticationException.class,
                        () -> refreshTokenService.refresh(rawToken)
                );

        // Assert
        assertThat(exception.getCode())
                .isEqualTo("AUTH_REFRESH_TOKEN_EXPIRED");

        verify(refreshToken, never())
                .getAccount();

        verify(jwtTokenService, never())
                .generateAccessToken(any());
    }

    // Disabled and locked account
    @Test
    void refresh_shouldThrow_whenAccountIsDisabled() {

        // Arrange
        String rawToken = "raw-token";

        when(tokenHasher.hash(rawToken))
                .thenReturn("hashed-token");

        when(refreshTokenRepository.findByTokenHash("hashed-token"))
                .thenReturn(Optional.of(refreshToken));

        when(refreshToken.isRevoked())
                .thenReturn(false);

        when(refreshToken.isExpired())
                .thenReturn(false);

        when(refreshToken.getAccount())
                .thenReturn(account);

        when(account.isDisabled())
                .thenReturn(true);

        // Act
        AuthenticationException exception =
                assertThrows(
                        AuthenticationException.class,
                        () -> refreshTokenService.refresh(rawToken)
                );

        // Assert
        assertThat(exception.getCode())
                .isEqualTo("AUTH_ACCOUNT_DISABLED");

        verify(jwtTokenService, never())
                .generateAccessToken(any());
    }

    @Test
    void refresh_shouldThrow_whenAccountIsLocked() {

        // Arrange
        String rawToken = "raw-token";

        when(tokenHasher.hash(rawToken))
                .thenReturn("hashed-token");

        when(refreshTokenRepository.findByTokenHash("hashed-token"))
                .thenReturn(Optional.of(refreshToken));

        when(refreshToken.isRevoked())
                .thenReturn(false);

        when(refreshToken.isExpired())
                .thenReturn(false);

        when(refreshToken.getAccount())
                .thenReturn(account);

        when(account.isDisabled())
                .thenReturn(false);

        when(account.isLocked())
                .thenReturn(true);

        // Act
        AuthenticationException exception =
                assertThrows(
                        AuthenticationException.class,
                        () -> refreshTokenService.refresh(rawToken)
                );

        // Assert
        assertThat(exception.getCode())
                .isEqualTo("AUTH_ACCOUNT_LOCKED");

        verify(jwtTokenService, never())
                .generateAccessToken(any());
    }
}



