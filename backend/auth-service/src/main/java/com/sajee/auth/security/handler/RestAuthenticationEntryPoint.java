package com.sajee.auth.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final SecurityErrorResponseWriter responseWriter;

    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException
    ) throws IOException {

        log.debug("Authentication required for {} {}", request.getMethod(), request.getRequestURI());

        responseWriter.write(
                response,
                HttpServletResponse.SC_UNAUTHORIZED,
                "AUTH_UNAUTHORIZED",
                "Authentication is required to access this resource.",
                request.getRequestURI()
        );
    }
}
