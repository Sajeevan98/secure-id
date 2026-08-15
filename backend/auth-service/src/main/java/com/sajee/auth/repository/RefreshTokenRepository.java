package com.sajee.auth.repository;

import com.sajee.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUuid(UUID uuid);

    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
