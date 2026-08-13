package com.sajee.auth.account.register;

import com.sajee.auth.entity.Account;
import com.sajee.auth.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@Testcontainers
@SpringBootTest
public class AccountRepositoryIntegrationTest {

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

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
    }

    @Test
    void shouldPersistAccount() {

        Account account = Account.builder()
                .username("sajeevan")
                .email("sajeevan@example.com")
                .passwordHash("hashed-password")
                .build();

        Account saved = accountRepository.save(account);

        assertThat(saved.getId())
                .isNotNull();
        assertThat(saved.getUuid())
                .isNotNull();
        assertThat(saved.getUsername())
                .isEqualTo("sajeevan");
        assertThat(saved.getEmail())
                .isEqualTo("sajeevan@example.com");
        assertThat(saved.isEmailVerified())
                .isFalse();
    }

    @Test
    void shouldFindAccountByEmail() {

        Account account = Account.builder()
                .username("sajeevan")
                .email("sajeevan@example.com")
                .passwordHash("hashed-password")
                .build();

        accountRepository.save(account);

        assertThat(accountRepository.findByEmail("sajeevan@example.com"))
                .isPresent();
    }

    @Test
    void shouldFindAccountByUuid() {

        Account account = Account.builder()
                .username("sajeevan")
                .email("sajeevan@example.com")
                .passwordHash("hashed-password")
                .build();

        Account saved = accountRepository.save(account);

        assertThat(accountRepository.findByUuid(saved.getUuid()))
                .isPresent();
    }

    @Test
    void shouldExistsAccountByUsername() {

        Account account = Account.builder()
                .username("sajeevan")
                .email("sajeevan@example.com")
                .passwordHash("hashed-password")
                .build();

        accountRepository.save(account);

        assertThat(accountRepository.existsByUsername("sajeevan"))
                .isTrue();
    }

    @Test
    void shouldEnforceUniqueEmail() {

        Account first = Account.builder()
                .username("user1")
                .email("same@example.com")
                .passwordHash("hashed-password")
                .build();

        Account second = Account.builder()
                .username("user2")
                .email("same@example.com")
                .passwordHash("hashed-password")
                .build();

        accountRepository.saveAndFlush(first);

        assertThatThrownBy(() -> accountRepository.saveAndFlush(second))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldEnforceUniqueUsername() {

        Account first = Account.builder()
                .username("sameuser")
                .email("one@example.com")
                .passwordHash("hashed-password")
                .build();

        Account second = Account.builder()
                .username("sameuser")
                .email("two@example.com")
                .passwordHash("hashed-password")
                .build();

        accountRepository.saveAndFlush(first);

        assertThatThrownBy(() -> accountRepository.saveAndFlush(second))
                .isInstanceOf(Exception.class);
    }

}
