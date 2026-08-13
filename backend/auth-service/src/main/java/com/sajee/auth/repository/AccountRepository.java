package com.sajee.auth.repository;

import com.sajee.auth.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<Account> findByUuid(UUID uuid);

    Optional<Account> findByEmail(String email);

}
