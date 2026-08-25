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
        name = "password_reset_tokens",
        indexes = {
                @Index(
                        name = "idx_password_reset_tokens_account_id",
                        columnList = "account_id"
                ),
                @Index(
                        name = "idx_password_reset_tokens_token_hash",
                        columnList = "token_hash"
                ),
                @Index(
                        name = "idx_password_reset_tokens_expires_at",
                        columnList = "expires_at"
                )
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken extends AuditableEntity {

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
            foreignKey = @ForeignKey(
                    name = "fk_password_reset_tokens_account"
            )
    )
    private Account account;

    @Column(
            name = "token_hash",
            nullable = false,
            unique = true,
            length = 64
    )
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;


    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean isActive() {
        return !isExpired() && !isUsed();
    }

    public void markUsed() {
        if (usedAt == null) {
            usedAt = Instant.now();
        }
    }
}
