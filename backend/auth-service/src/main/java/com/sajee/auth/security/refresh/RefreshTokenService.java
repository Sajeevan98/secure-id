package com.sajee.auth.security.refresh;

import com.sajee.auth.common.exception.RefreshTokenException;
import com.sajee.auth.dto.response.RefreshTokenResponse;
import com.sajee.auth.entity.Account;
import com.sajee.auth.entity.RefreshToken;
import com.sajee.auth.enums.RefreshTokenRevocationReason;
import com.sajee.auth.repository.RefreshTokenRepository;
import com.sajee.auth.security.jwt.JwtProperties;
import com.sajee.auth.security.jwt.JwtToken;
import com.sajee.auth.security.jwt.JwtTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final JwtProperties jwtProperties;

    private final RefreshTokenGenerator tokenGenerator;
    private final RefreshTokenHasher tokenHasher;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenFamilyService refreshTokenFamilyService;


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
                .familyId(UUID.randomUUID())
                .build();

        log.debug("Refresh token created.");
        RefreshToken saved = refreshTokenRepository.save(refreshToken);

        return new GenerateRefreshTokenResponse(
                rawToken,
                saved.getUuid(),
                saved.getExpiresAt()
        );
    }

    public RefreshTokenResponse rotate(
            String rawRefreshToken, String ipAddress, String userAgent) {

        String tokenHash = tokenHasher.hash(rawRefreshToken);

        RefreshToken currentToken = refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new RefreshTokenException(
                        "AUTH_INVALID_REFRESH_TOKEN", "Invalid refresh token."
                ));

        // Expired token
        if (currentToken.isExpired()) {

            log.debug("Refresh token is Expired");
            throw new RefreshTokenException("AUTH_REFRESH_TOKEN_EXPIRED", "Refresh token has expired.");
        }

        // Reuse detection
        if (currentToken.isRevoked()) {

            log.warn(
                    "Refresh token reuse detected. Token: {}, Family: {}",
                    currentToken.getUuid(),
                    currentToken.getFamilyId()
            );

            refreshTokenFamilyService.revokeFamily(currentToken.getFamilyId());

            throw new RefreshTokenException("AUTH_REFRESH_TOKEN_REUSED", "Refresh token reuse detected.");
        }

        // Current token is valid
        Account account = currentToken.getAccount();

        // Generate replacement refresh token
        String newRawToken = tokenGenerator.generate();

        String newTokenHash = tokenHasher.hash(newRawToken);

        Instant expiresAt = Instant.now()
                .plus(jwtProperties.refreshTokenTtl());

        RefreshToken replacement = RefreshToken.builder()
                .account(account)
                .tokenHash(newTokenHash)
                .expiresAt(expiresAt)
                .createdIp(ipAddress)
                .userAgent(userAgent)
                .familyId(currentToken.getFamilyId())
                .build();

        RefreshToken savedReplacement = refreshTokenRepository.save(replacement);

        // Consume old token
        currentToken.markReplacedBy(savedReplacement);

        JwtToken accessToken =
                jwtTokenService.generateAccessToken(account);

        return new RefreshTokenResponse(
                accessToken.value(),
                newRawToken,
                "Bearer",
                accessToken.expiresAt()
        );
    }
}
