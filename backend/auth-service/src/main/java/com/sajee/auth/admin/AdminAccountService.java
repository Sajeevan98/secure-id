package com.sajee.auth.admin;

import com.sajee.auth.common.exception.BusinessException;
import com.sajee.auth.entity.Account;
import com.sajee.auth.enums.Role;
import com.sajee.auth.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminAccountService {

    private final AccountRepository accountRepository;

    public void changeRole(UUID accountUuid, Role role) {

        Account account = accountRepository.findByUuid(accountUuid)
                .orElseThrow(() -> new BusinessException(
                        "AUTH_ACCOUNT_NOT_FOUND",
                        "Account not found."
                ));

        account.changeRole(role);
    }
}
