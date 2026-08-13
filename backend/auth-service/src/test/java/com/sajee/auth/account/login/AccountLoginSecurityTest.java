package com.sajee.auth.account.login;

import com.sajee.auth.entity.Account;
import com.sajee.auth.enums.AccountStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountLoginSecurityTest {

    // Failed attempts
    @Test
    void shouldIncrementFailedLoginAttempts() {

        Account account = Account.builder()
                .username("sajeevan")
                .email("sajeevan@example.com")
                .passwordHash("hash@123")
                .build();

        account.recordFailedLoginAttempt();

        assertThat(account.getFailedLoginAttempts())
                .isEqualTo(1);

        assertThat(account.getStatus())
                .isEqualTo(AccountStatus.ACTIVE);
    }

    // Fifth failed attempt locks account
    @Test
    void shouldLockAccountAfterFiveFailedAttempts() {

        Account account = Account.builder()
                .username("sajeevan")
                .email("sajeevan@example.com")
                .passwordHash("hash@123")
                .build();

        for (int i = 0; i < 5; i++) {
            account.recordFailedLoginAttempt();
        }

        assertThat(account.getFailedLoginAttempts())
                .isEqualTo(5);

        assertThat(account.getStatus())
                .isEqualTo(AccountStatus.LOCKED);

        assertThat(account.getLockedUntil())
                .isNotNull();
    }

    // Successful login resets attempts
    @Test
    void shouldResetFailedAttemptsAfterSuccessfulLogin() {

        Account account = Account.builder()
                .username("sajeevan")
                .email("sajeevan@example.com")
                .passwordHash("haslh@123")
                .build();

        account.recordFailedLoginAttempt();
        account.recordFailedLoginAttempt();

        account.recordSuccessfulLogin();

        assertThat(account.getFailedLoginAttempts())
                .isZero();

        assertThat(account.getLockedUntil())
                .isNull();
    }

    // Disabled accounts must stay disabled.
    @Test
    void shouldIdentifyDisabledAccount() {

        Account account = Account.builder()
                .username("sajeevan")
                .email("sajeevan@example.com")
                .passwordHash("haslh@123")
                .build();

        // Domain method for administrative status
        // Changes when implement the account administration.

        assertThat(account.isDisabled())
                .isFalse();
    }
}
