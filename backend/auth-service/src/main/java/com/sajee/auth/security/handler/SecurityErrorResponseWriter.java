package com.sajee.auth.security.handler;

import com.sajee.auth.common.api.ApiError;
import com.sajee.auth.common.api.ApiErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class SecurityErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public void write(
            HttpServletResponse response, int status, String code, String message, String request
    ) throws IOException {

        ApiError error = new ApiError(code, message, request, null);
        ApiErrorResponse errorResponse = ApiErrorResponse.of(error);

        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
