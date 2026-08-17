package com.sajee.auth.account.register;

import com.sajee.auth.common.exception.GlobalExceptionHandler;
import com.sajee.auth.controller.AuthController;
import com.sajee.auth.dto.request.RegisterRequest;
import com.sajee.auth.dto.response.RegisterResponse;
import com.sajee.auth.security.refresh.RefreshTokenService;
import com.sajee.auth.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
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

    @Test
    void shouldRegisterAccount() throws Exception {

        RegisterRequest registerRequest = new RegisterRequest(
                "sajeevan",
                "sajeevan@example.com",
                "StrongPassword!@#123"
        );

        RegisterResponse response = new RegisterResponse(
                UUID.randomUUID(),
                "sajeevan",
                "sajeevan@example.com",
                "ACTIVE",
                false
        );

        when(accountService.register(any(RegisterRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success")
                        .value(true))
                .andExpect(jsonPath("$.data.username")
                        .value("sajeevan"))
                .andExpect(jsonPath("$.data.email")
                        .value("sajeevan@example.com"))
                .andExpect(jsonPath("$.data.status")
                        .value("ACTIVE"))
                .andExpect(jsonPath("$.data.emailVerified")
                        .value(false));
    }

    @Test
    void shouldRejectInvalidRequest() throws Exception {

        RegisterRequest registerRequest = new RegisterRequest(
                "",
                "invalid-email",
                "123"
        );

        mockMvc.perform(post("/api/v1/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success")
                        .value(false))
                .andExpect(jsonPath("$.error.code")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.message")
                        .value("Request validation failed."))
                .andExpect(jsonPath("$.error.path")
                        .value("/api/v1/auth/registration"))
                .andExpect(jsonPath("$.error.errors")
                        .exists());
    }
}
