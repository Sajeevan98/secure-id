package com.sajee.auth.entity;

import com.sajee.auth.common.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "email_verification_tokens",
        indexes = {
                @Index(
                        name = "idx_email_verification_tokens_account_id",
                        columnList = "account_id"
                ),
                @Index(
                        name = "idx_email_verification_tokens_token_hash",
                        columnList = "token_hash"
                ),
                @Index(
                        name = "idx_email_verification_tokens_expires_at",
                        columnList = "expires_at"
                )
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationToken extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "account_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_email_verification_tokens_account")
    )
    private Account account;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;


    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isVerified() {
        return verifiedAt != null;
    }

    public boolean isActive() {
        return !isExpired() && !isVerified();
    }

    public void markVerified() {
        if (verifiedAt == null) {
            verifiedAt = Instant.now();
        }
    }
}
