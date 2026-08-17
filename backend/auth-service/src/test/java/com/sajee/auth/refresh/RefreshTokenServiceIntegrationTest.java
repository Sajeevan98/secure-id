package com.sajee.auth.refresh;

import com.sajee.auth.dto.response.RefreshTokenResponse;
import com.sajee.auth.entity.Account;
import com.sajee.auth.entity.RefreshToken;
import com.sajee.auth.repository.AccountRepository;
import com.sajee.auth.repository.RefreshTokenRepository;
import com.sajee.auth.security.refresh.GenerateRefreshTokenResponse;
import com.sajee.auth.security.refresh.RefreshTokenHasher;
import com.sajee.auth.security.refresh.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
public class RefreshTokenServiceIntegrationTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RefreshTokenHasher tokenHasher;

    @BeforeEach
    void setUp(){
        refreshTokenRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    void create_shouldPersistRefreshToken() {

        // Arrange
        Account account = createAccount();

        // Act
        GenerateRefreshTokenResponse response = refreshTokenService.create(
                account,
                "127.0.0.1",
                "JUnit"
        );

        // Assert
        assertThat(response.token())
                .isNotBlank();

        assertThat(response.uuid())
                .isNotNull();

        assertThat(response.expiresAt())
                .isAfter(Instant.now());

        String hash = tokenHasher.hash(response.token());

        RefreshToken saved = refreshTokenRepository
                .findByTokenHash(hash)
                .orElseThrow();

        assertThat(saved.getUuid())
                .isEqualTo(response.uuid());

        assertThat(saved.getAccount().getId())
                .isEqualTo(account.getId());

        assertThat(saved.getTokenHash())
                .isEqualTo(hash);

        assertThat(saved.getCreatedIp())
                .isEqualTo("127.0.0.1");

        assertThat(saved.getUserAgent())
                .isEqualTo("JUnit");
    }

    @Test
    void refresh_shouldWorkWithPersistedToken() {

        // Arrange
        Account account = createAccount();

        GenerateRefreshTokenResponse generated = refreshTokenService
                .create(account, "127.0.0.1", "JUnit");

        // Act
        RefreshTokenResponse response = refreshTokenService.refresh(generated.token());

        // Assert
        assertThat(response)
                .isNotNull();

        assertThat(response.accessToken())
                .isNotBlank();

        assertThat(response.tokenType())
                .isEqualTo("Bearer");

        assertThat(response.expiresIn())
                .isAfter(Instant.now());
    }

    private Account createAccount() {

        Account account = Account.builder()
                .username("sajeevan")
                .email("sajeevan@example.com")
                .passwordHash("hash-password")
                .build();

        return accountRepository.save(account);
    }

}
