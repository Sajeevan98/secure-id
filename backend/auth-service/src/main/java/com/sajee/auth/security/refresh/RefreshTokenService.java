package com.sajee.auth.security.refresh;

import com.sajee.auth.common.exception.AuthenticationException;
import com.sajee.auth.dto.response.RefreshTokenResponse;
import com.sajee.auth.entity.Account;
import com.sajee.auth.entity.RefreshToken;
import com.sajee.auth.repository.RefreshTokenRepository;
import com.sajee.auth.security.jwt.JwtProperties;
import com.sajee.auth.security.jwt.JwtToken;
import com.sajee.auth.security.jwt.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final JwtProperties jwtProperties;

    private final RefreshTokenGenerator tokenGenerator;
    private final RefreshTokenHasher tokenHasher;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenService jwtTokenService;


    public GenerateRefreshTokenResponse create(Account account, String ipAddress, String userAgent) {

        String rawToken = tokenGenerator.generate();

        String tokenHash = tokenHasher.hash(rawToken);

        Instant expiresAt = Instant.now()
                .plus(jwtProperties.refreshTokenTtl());

        RefreshToken refreshToken = RefreshToken.builder()
                .account(account)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .createdIp(ipAddress)
                .userAgent(userAgent)
                .build();

        RefreshToken saved = refreshTokenRepository.save(refreshToken);

        return new GenerateRefreshTokenResponse(
                rawToken,
                saved.getUuid(),
                saved.getExpiresAt()
        );
    }


    public RefreshTokenResponse refresh(String rawRefreshToken) {

        String tokenHash = tokenHasher.hash(rawRefreshToken);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() ->
                        new AuthenticationException("AUTH_INVALID_REFRESH_TOKEN", "Invalid refresh token.")
                );

        if (refreshToken.isRevoked()) {
            throw new AuthenticationException("AUTH_REFRESH_TOKEN_REVOKED", "Refresh token has been revoked.");
        }

        if (refreshToken.isExpired()) {
            throw new AuthenticationException("AUTH_REFRESH_TOKEN_EXPIRED", "Refresh token has expired.");
        }

        Account account = refreshToken.getAccount();

        if (account.isDisabled()) {
            throw new AuthenticationException("AUTH_ACCOUNT_DISABLED", "Account is disabled.");
        }

        if (account.isLocked()) {
            throw new AuthenticationException("AUTH_ACCOUNT_LOCKED", "Account is temporarily locked.");
        }

        JwtToken accessToken = jwtTokenService.generateAccessToken(account);

        return new RefreshTokenResponse(
                accessToken.value(),
                "Bearer",
                accessToken.expiresAt()
        );
    }
}
