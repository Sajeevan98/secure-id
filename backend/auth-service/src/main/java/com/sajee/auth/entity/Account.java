package com.sajee.auth.entity;

import com.sajee.auth.common.persistence.AuditableEntity;
import com.sajee.auth.enums.AccountStatus;
import com.sajee.auth.security.login.LoginSecurityPolicy;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid = UUID.randomUUID();

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Builder.Default
    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private Instant lockedUntil;


    // =============== Helper methods for Login-State ===============
    public boolean isLocked() {

        if (status != AccountStatus.LOCKED) {
            return false;
        }

        if (lockedUntil == null) {
            return true;
        }
        return Instant.now().isBefore(lockedUntil);
    }

    public boolean isLockExpired() {
        return status == AccountStatus.LOCKED
                && lockedUntil != null
                && !Instant.now().isBefore(lockedUntil);
    }

    public void unlockIfLockExpired() {

        if (!isLockExpired()) {
            return;
        }
        status = AccountStatus.ACTIVE;
        failedLoginAttempts = 0;
        lockedUntil = null;
    }

    public boolean isDisabled() {
        return status == AccountStatus.DISABLED;
    }

    public void recordFailedLoginAttempt() {
        failedLoginAttempts++;

        log.debug("Failed login attempts: {}", failedLoginAttempts);

        if (failedLoginAttempts >= LoginSecurityPolicy.MAX_FAILED_ATTEMPTS) {
            status = AccountStatus.LOCKED;
            lockedUntil = Instant.now().plus(LoginSecurityPolicy.LOCK_DURATION);
        }
    }

    public void recordSuccessfulLogin() {
        failedLoginAttempts = 0;
        lockedUntil = null;
        log.debug("Login attempts reset: {}", failedLoginAttempts);
    }
}
