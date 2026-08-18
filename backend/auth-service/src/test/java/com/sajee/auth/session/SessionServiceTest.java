package com.sajee.auth.session;

import com.sajee.auth.common.exception.AuthenticationException;
import com.sajee.auth.repository.RefreshTokenRepository;
import com.sajee.auth.security.session.SessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class SessionServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private SessionService sessionService;

    @Test
    void shouldRejectSessionBelongingToAnotherAccount() {

        UUID bobUuid = UUID.randomUUID();
        UUID aliceSessionUuid = UUID.randomUUID();

        given(refreshTokenRepository
                .findByUuidAndAccountUuid(aliceSessionUuid, bobUuid))
                .willReturn(Optional.empty());

        assertThatThrownBy(() ->
                sessionService.revokeSession(
                        bobUuid,
                        aliceSessionUuid
                )
        )
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Session not found.");
    }
}
