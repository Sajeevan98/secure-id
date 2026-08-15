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
        name = "refresh_tokens",
        indexes = {
                @Index(
                        name = "idx_refresh_tokens_account_id",
                        columnList = "account_id"
                ),
                @Index(
                        name = "idx_refresh_tokens_token_hash",
                        columnList = "token_hash"
                ),
                @Index(
                        name = "idx_refresh_tokens_expires_at",
                        columnList = "expires_at"
                )
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken extends AuditableEntity {

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
            foreignKey = @ForeignKey(name = "fk_refresh_tokens_account")
    )
    private Account account;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "created_ip", length = 45)
    private String createdIp;

    @Column(name = "user_agent", length = 1000)
    private String userAgent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "replaced_by_token_id",
            foreignKey = @ForeignKey(name = "fk_refresh_tokens_replaced_by")
    )
    private RefreshToken replacedByToken;


    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isActive() {
        return !isExpired() && !isRevoked();
    }

    public void revoke() {
        if (revokedAt == null) {
            revokedAt = Instant.now();
        }
    }

    public void replaceWith(RefreshToken replacement) {
        this.revokedAt = Instant.now();
        this.replacedByToken = replacement;
    }
}
