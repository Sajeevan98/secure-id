package com.sajee.auth.refresh;

import com.sajee.auth.entity.Account;
import com.sajee.auth.entity.RefreshToken;
import com.sajee.auth.repository.AccountRepository;
import com.sajee.auth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
public class RefreshTokenRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
    }

    @Test
    @Transactional
    void shouldPersistAndFindByTokenHash() {

        Account account = Account.builder()
                .username("sajeevan")
                .email("sajeevan@example.com")
                .passwordHash("hash-password")
                .build();

        Account savedAccount = accountRepository.saveAndFlush(account);

        RefreshToken token = RefreshToken.builder()
                .account(savedAccount)
                .tokenHash("abcdef123456")
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        refreshTokenRepository.saveAndFlush(token);

        RefreshToken saved = refreshTokenRepository
                .findByTokenHash("abcdef123456")
                .orElseThrow();

        assertThat(saved.getUuid()).isNotNull();
        assertThat(saved.getAccount().getUuid())
                .isEqualTo(account.getUuid());
    }
}
