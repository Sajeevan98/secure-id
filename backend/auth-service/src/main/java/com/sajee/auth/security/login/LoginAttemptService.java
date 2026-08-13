package com.sajee.auth.security.login;

import com.sajee.auth.entity.Account;
import com.sajee.auth.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * I created this service for "preventing rolled back transaction".
 * <p>
 * 'failedLoginAttempts' is not increased on table, when login is failed.
 * Because 'AuthenticationException' extends my BusinessException,
 * and assuming BusinessException is a runtime exception, Spring rolls back the transaction
 * <p>
 * Here, I introduced 'REQUIRES_NEW'. because it creates a separate transaction.
 * <p>
 * Here, I received 'accountUuid' instead of 'account' from parent class(AccountServiceImpl.java).
 * If I pass account, Account was loaded in the outer transaction(this class), but REQUIRES_NEW creates another persistence context,
 * So, I prefer that the new transaction reload the account.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final AccountRepository accountRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedAttempt(UUID accountUuid) {

        Account account = accountRepository.findByUuid(accountUuid)
                .orElseThrow();

        account.recordFailedLoginAttempt();
        log.debug("Password doesn't match to account {}", account.getEmail());
        accountRepository.save(account);
    }

    @Transactional
    public void recordSuccessfulLogin(UUID accountUuid) {

        Account account = accountRepository.findByUuid(accountUuid)
                .orElseThrow();

        account.recordSuccessfulLogin();
        log.debug("login successfully with {}", account.getEmail());
        accountRepository.save(account);
    }
}
