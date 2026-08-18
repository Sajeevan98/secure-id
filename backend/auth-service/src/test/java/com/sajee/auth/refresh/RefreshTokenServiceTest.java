package com.sajee.auth.refresh;

import com.sajee.auth.common.exception.RefreshTokenException;
import com.sajee.auth.entity.Account;
import com.sajee.auth.entity.RefreshToken;
import com.sajee.auth.enums.RefreshTokenRevocationReason;
import com.sajee.auth.repository.RefreshTokenRepository;
import com.sajee.auth.security.jwt.JwtProperties;
import com.sajee.auth.security.jwt.JwtTokenService;
import com.sajee.auth.security.refresh.*;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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

    @Mock
    private RefreshTokenFamilyService refreshTokenFamilyService;

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
    void shouldRevokeEntireFamilyWhenRefreshTokenIsReused() {

        UUID familyId = UUID.randomUUID();

        RefreshToken token1 = RefreshToken.builder()
                .uuid(UUID.randomUUID())
                .familyId(familyId)
                .tokenHash("hash-1")
                .expiresAt(Instant.now().plus(Duration.ofDays(30)))
                .build();

        token1.revoke(RefreshTokenRevocationReason.ROTATED);

        when(tokenHasher.hash("raw-token"))
                .thenReturn("hash-1");

        when(refreshTokenRepository.findByTokenHash("hash-1"))
                .thenReturn(Optional.of(token1));

        assertThatThrownBy(() ->
                refreshTokenService.rotate(
                        "raw-token",
                        "127.0.0.1",
                        "JUnit"
                )
        )
                .isInstanceOf(RefreshTokenException.class)
                .hasMessage("Refresh token reuse detected.");

        verify(refreshTokenFamilyService)
                .revokeFamily(familyId);
    }


    @Test
    void shouldRevokeRefreshTokenOnLogout() {

        RefreshToken token = RefreshToken.builder()
                .uuid(UUID.randomUUID())
                .familyId(UUID.randomUUID())
                .tokenHash("hash-1")
                .expiresAt(Instant.now().plus(Duration.ofDays(30)))
                .build();

        given(tokenHasher.hash("raw-token"))
                .willReturn("hash-1");

        given(refreshTokenRepository.findByTokenHash("hash-1"))
                .willReturn(Optional.of(token));

        refreshTokenService.logout("raw-token");

        assertThat(token.isRevoked())
                .isTrue();

        assertThat(token.getRevocationReason())
                .isEqualTo(RefreshTokenRevocationReason.LOGOUT);
    }

    @Test
    void shouldRejectInvalidRefreshTokenOnLogout() {

        given(tokenHasher.hash("invalid-token"))
                .willReturn("unknown-hash");

        given(refreshTokenRepository.findByTokenHash("unknown-hash"))
                .willReturn(Optional.empty());

        assertThatThrownBy(() ->
                refreshTokenService.logout("invalid-token")
        )
                .isInstanceOf(RefreshTokenException.class)
                .hasMessage("Invalid refresh token.");
    }

    @Test
    void shouldRejectAlreadyRevokedRefreshTokenOnLogout() {

        RefreshToken token = RefreshToken.builder()
                .uuid(UUID.randomUUID())
                .familyId(UUID.randomUUID())
                .tokenHash("hash-1")
                .expiresAt(Instant.now().plus(Duration.ofDays(30)))
                .build();

        token.revoke(RefreshTokenRevocationReason.ROTATED);

        given(tokenHasher.hash("raw-token"))
                .willReturn("hash-1");

        given(refreshTokenRepository.findByTokenHash("hash-1"))
                .willReturn(Optional.of(token));

        assertThatThrownBy(() ->
                refreshTokenService.logout("raw-token")
        )
                .isInstanceOf(RefreshTokenException.class)
                .hasMessage("Refresh token has already been revoked.");
    }
}



