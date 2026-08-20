package com.sajee.auth.email;

import com.sajee.auth.entity.EmailVerificationToken;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class EmailVerificationTokenTest {

    @Test
    void shouldBeActiveWhenNotExpiredAndNotVerified() {

        EmailVerificationToken token = EmailVerificationToken.builder()
                .expiresAt(Instant.now().plusSeconds(900))
                .build();

        assertThat(token.isActive()).isTrue();
        assertThat(token.isExpired()).isFalse();
        assertThat(token.isVerified()).isFalse();
    }

    @Test
    void shouldBeExpiredWhenExpirationIsInThePast() {

        EmailVerificationToken token = EmailVerificationToken.builder()
                .expiresAt(Instant.now().minusSeconds(1))
                .build();

        assertThat(token.isExpired()).isTrue();
        assertThat(token.isActive()).isFalse();
    }

    @Test
    void shouldBeVerifiedAfterMarkingVerified() {

        EmailVerificationToken token = EmailVerificationToken.builder()
                .expiresAt(Instant.now().plusSeconds(900))
                .build();

        token.markVerified();

        assertThat(token.isVerified()).isTrue();
        assertThat(token.isActive()).isFalse();
        assertThat(token.getVerifiedAt()).isNotNull();
    }
}
