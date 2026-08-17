package com.sajee.auth.security.refresh;

import com.sajee.auth.entity.Account;
import com.sajee.auth.entity.RefreshToken;
import com.sajee.auth.repository.RefreshTokenRepository;
import com.sajee.auth.security.jwt.JwtProperties;
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
}
