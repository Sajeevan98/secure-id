package com.sajee.auth.service;

import com.sajee.auth.common.exception.ConflictException;
import com.sajee.auth.dto.request.RegisterRequest;
import com.sajee.auth.dto.response.RegisterResponse;
import com.sajee.auth.entity.Account;
import com.sajee.auth.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public RegisterResponse register(RegisterRequest request) {

        String username = request.username().trim().toLowerCase(Locale.ROOT);
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String passwordHash = passwordEncoder.encode(request.password());

        if (accountRepository.existsByUsername(username)) {

            log.debug("{} username already exists", username);
            throw new ConflictException("AUTH_USERNAME_ALREADY_EXISTS", "An account with this username already exists");
        }

        if (accountRepository.existsByEmail(email)){

            log.debug("{} email already exists", email);
            throw new ConflictException("AUTH_EMAIL_ALREADY_EXISTS", "An account with this email already exists");
        }

        Account account = Account.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordHash)
                .build();

        Account registerAccount = accountRepository.save(account);

        return RegisterResponse.from(registerAccount);
    }
}
