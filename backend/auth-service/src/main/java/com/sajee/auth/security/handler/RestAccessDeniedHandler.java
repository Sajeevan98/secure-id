package com.sajee.auth.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final SecurityErrorResponseWriter responseWriter;

    @Override
    public void handle(
            HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException
    ) throws IOException {

        log.debug("Access denied for request: {} {}", request.getMethod(), request.getRequestURI());

        responseWriter.write(
                response,
                HttpServletResponse.SC_FORBIDDEN,
                "AUTH_ACCESS_DENIED",
                "You do not have permission to access this resource.",
                request.getRequestURI()
        );
    }
}
