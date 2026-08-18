package com.sajee.auth.security.session;

import com.sajee.auth.common.exception.AuthenticationException;
import com.sajee.auth.dto.response.SessionResponse;
import com.sajee.auth.entity.RefreshToken;
import com.sajee.auth.enums.RefreshTokenRevocationReason;
import com.sajee.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional(readOnly = true)
    public List<SessionResponse> getActiveSessions(UUID accountUuid) {

        return refreshTokenRepository
                .findByAccountUuidAndRevokedAtIsNullOrderByCreatedAtDesc(accountUuid)
                .stream()
                .filter(RefreshToken::isActive)
                .map(SessionResponse::from)
                .toList();
    }

    public void revokeSession(UUID accountUuid, UUID sessionUuid) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByUuidAndAccountUuid(sessionUuid, accountUuid)
                .orElseThrow(() -> new AuthenticationException(
                        "AUTH_SESSION_NOT_FOUND", "Session not found."
                ));

        if (refreshToken.isRevoked()) {
            throw new AuthenticationException(
                    "AUTH_SESSION_ALREADY_REVOKED", "Session has already been revoked."
            );
        }

        refreshToken.revoke(RefreshTokenRevocationReason.LOGOUT);
    }
}