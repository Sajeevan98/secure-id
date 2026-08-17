package com.sajee.auth.account.login;

import com.sajee.auth.common.exception.AuthenticationException;
import com.sajee.auth.common.exception.GlobalExceptionHandler;
import com.sajee.auth.controller.AuthController;
import com.sajee.auth.dto.request.LoginRequest;
import com.sajee.auth.dto.response.LoginResponse;
import com.sajee.auth.security.refresh.RefreshTokenService;
import com.sajee.auth.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private AccountService accountService;

    // Successful login
    @Test
    void shouldLoginSuccessfully() throws Exception {

        LoginRequest request = new LoginRequest(
                "sajeevan@example.com",
                "StrongPassword123!"
        );

        LoginResponse response = new LoginResponse(
                "mock-access-token",
                "mock-refresh-token",
                "Bearer",
                900
        );

        when(accountService.login(
                any(LoginRequest.class),
                anyString(),
                eq("JUnit")
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .header("User-Agent", "JUnit")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success")
                        .value(true))
                .andExpect(jsonPath("$.data.accessToken")
                        .value("mock-access-token"))
                .andExpect(jsonPath("$.data.refreshToken")
                        .value("mock-refresh-token"))
                .andExpect(jsonPath("$.data.tokenType")
                        .value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn")
                        .value(900));

        verify(accountService).login(
                any(LoginRequest.class),
                anyString(),
                eq("JUnit")
        );
    }

    // Invalid credentials
    @Test
    void shouldReturnUnauthorizedForInvalidCredentials() throws Exception {

        LoginRequest request = new LoginRequest(
                "sajeevan@example.com",
                "WrongPassword123!"
        );

        when(accountService.login(
                any(LoginRequest.class),
                anyString(),
                eq("JUnit")
        )).thenThrow(
                new AuthenticationException("AUTH_INVALID_CREDENTIALS", "Invalid email or password.")
        );

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .header("User-Agent", "JUnit")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success")
                        .value(false))
                .andExpect(jsonPath("$.error.code")
                        .value("AUTH_INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.error.message")
                        .value("Invalid email or password."))
                .andExpect(jsonPath("$.error.path")
                        .value("/api/v1/auth/login"));
    }

    // Locked account
    @Test
    void shouldReturnLockedForLockedAccount() throws Exception {

        LoginRequest request = new LoginRequest(
                "sajeevan@example.com",
                "StrongPassword123!"
        );

        when(accountService.login(
                any(LoginRequest.class),
                anyString(),
                eq("JUnit")
        )).thenThrow(
                new AuthenticationException("AUTH_ACCOUNT_LOCKED", "Account is temporarily locked.")
        );

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .header("User-Agent", "JUnit")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code")
                        .value("AUTH_ACCOUNT_LOCKED"));
    }

    // Disabled account
    @Test
    void shouldReturnForbiddenForDisabledAccount() throws Exception {

        LoginRequest request = new LoginRequest(
                "sajeevan@example.com",
                "StrongPassword123!"
        );

        when(accountService.login(
                any(LoginRequest.class),
                anyString(),
                eq("JUnit")
        )).thenThrow(
                new AuthenticationException("AUTH_ACCOUNT_DISABLED", "Account is disabled.")
        );

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .header("User-Agent", "JUnit")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code")
                        .value("AUTH_ACCOUNT_DISABLED"));
    }
}
