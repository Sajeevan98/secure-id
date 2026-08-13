package com.sajee.auth.account.register;

import com.sajee.auth.common.exception.ConflictException;
import com.sajee.auth.dto.request.RegisterRequest;
import com.sajee.auth.dto.response.RegisterResponse;
import com.sajee.auth.entity.Account;
import com.sajee.auth.repository.AccountRepository;
import com.sajee.auth.service.AccountServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountRegistrationServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void shouldRegisterAccount() {

        RegisterRequest registerRequest = new RegisterRequest(
                "Sajeevan",
                "SAJEEVAN@Example.COM",
                "StrongPassword!@#123"
        );

        when(accountRepository.existsByUsername("sajeevan"))
                .thenReturn(false);
        when(accountRepository.existsByEmail("sajeevan@example.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("StrongPassword!@#123"))
                .thenReturn("hashed-password");

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RegisterResponse response = accountService.register(registerRequest);

        assertThat(response.username())
                .isEqualTo("sajeevan");
        assertThat(response.email())
                .isEqualTo("sajeevan@example.com");
        assertThat(response.status())
                .isEqualTo("ACTIVE");
        assertThat(response.emailVerified())
                .isFalse();

        verify(passwordEncoder)
                .encode("StrongPassword!@#123");
        verify(accountRepository)
                .save(any(Account.class));
    }

    @Test
    void shouldRejectDuplicateUsername() {

        RegisterRequest registerRequest = new RegisterRequest(
                "sajeevan",
                "sajeevan@example.com",
                "StrongPassword!@#123"
        );

        when(accountRepository.existsByUsername("sajeevan"))
                .thenReturn(true);

        assertThatThrownBy(() -> accountService.register(registerRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("An account with this username already exists");

        verify(accountRepository, never())
                .save(any(Account.class));
    }

    @Test
    void shouldRejectDuplicateEmail() {

        RegisterRequest registerRequest = new RegisterRequest(
                "sajeevan",
                "sajeevan@example.com",
                "StrongPassword!@#123"
        );

        when(accountRepository.existsByUsername("sajeevan"))
                .thenReturn(false);
        when(accountRepository.existsByEmail("sajeevan@example.com"))
                .thenReturn(true);

        assertThatThrownBy(() -> accountService.register(registerRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("An account with this email already exists");

        verify(accountRepository, never())
                .save(any(Account.class));
    }

    @Test
    void shouldHashPasswordBeforeSaving() {

        RegisterRequest registerRequest = new RegisterRequest(
                "sajeevan",
                "sajeevan@example.com",
                "StrongPassword!@#123"
        );

        when(accountRepository.existsByUsername("sajeevan"))
                .thenReturn(false);
        when(accountRepository.existsByEmail("sajeevan@example.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("StrongPassword!@#123"))
                .thenReturn("argon2-hashed-value");

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        accountService.register(registerRequest);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        Account savedAccount = captor.getValue();

        assertThat(savedAccount.getPasswordHash())
                .isEqualTo("argon2-hashed-value");
        assertThat(savedAccount.getPasswordHash())
                .isNotEqualTo("StrongPassword123!");
    }

    @Test
    void shouldCreateActiveUnverifiedAccount() {

        RegisterRequest registerRequest = new RegisterRequest(
                "sajeevan",
                "sajeevan@example.com",
                "StrongPassword!@#123"
        );

        when(accountRepository.existsByUsername("sajeevan"))
                .thenReturn(false);
        when(accountRepository.existsByEmail("sajeevan@example.com"))
                .thenReturn(false);
        when(passwordEncoder.encode(anyString()))
                .thenReturn("hashed-password");

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RegisterResponse response = accountService.register(registerRequest);

        assertThat(response.status())
                .isEqualTo("ACTIVE");

        assertThat(response.emailVerified())
                .isFalse();
    }
}
