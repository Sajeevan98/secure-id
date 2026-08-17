package com.sajee.auth.refresh;

import com.sajee.auth.entity.Account;
import com.sajee.auth.entity.RefreshToken;
import com.sajee.auth.repository.AccountRepository;
import com.sajee.auth.repository.RefreshTokenRepository;
import com.sajee.auth.security.refresh.GenerateRefreshTokenResponse;
import com.sajee.auth.security.refresh.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RefreshTokenServiceTest {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    void shouldGenerateAndPersistRefreshToken() {

        Account account = Account.builder()
                .username("sajeevan")
                .email("sajeevan@example.com")
                .passwordHash("hash-password")
                .build();

        // Persist Account first
        account = accountRepository.save(account);

        GenerateRefreshTokenResponse response =
                refreshTokenService.create(
                        account,
                        "127.0.0.1",
                        "JUnit"
                );

        assertThat(response.token())
                .isNotBlank();
        assertThat(response.uuid())
                .isNotNull();
        assertThat(response.expiresAt())
                .isAfter(Instant.now());

        RefreshToken saved = refreshTokenRepository
                .findByUuid(response.uuid())
                .orElseThrow();

        assertThat(saved.getTokenHash())
                .isNotEqualTo(response.token());
        assertThat(saved.getTokenHash())
                .hasSize(64);
        assertThat(saved.getCreatedIp())
                .isEqualTo("127.0.0.1");
        assertThat(saved.getUserAgent())
                .isEqualTo("JUnit");
        assertThat(saved.getAccount().getId())
                .isEqualTo(account.getId());
    }
}