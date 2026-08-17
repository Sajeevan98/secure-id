package com.sajee.auth.security.refresh;

import com.sajee.auth.entity.RefreshToken;
import com.sajee.auth.enums.RefreshTokenRevocationReason;
import com.sajee.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenFamilyService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeFamily(UUID familyId) {

        List<RefreshToken> familyTokens =
                refreshTokenRepository.findAllByFamilyId(familyId);

        for (RefreshToken token : familyTokens) {
            token.revoke(RefreshTokenRevocationReason.REUSE_DETECTED);
        }

        log.warn(
                "Refresh token family {} revoked due to token reuse. Tokens revoked: {}",
                familyId,
                familyTokens.size()
        );
    }
}
