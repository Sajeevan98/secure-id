package com.sajee.auth.security.config;

import com.sajee.auth.security.email.EmailVerificationProperties;
import com.sajee.auth.security.handler.RestAccessDeniedHandler;
import com.sajee.auth.security.handler.RestAuthenticationEntryPoint;
import com.sajee.auth.security.jwt.JwtAuthenticationConverter;
import com.sajee.auth.security.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, EmailVerificationProperties.class})
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**")
                        .permitAll()
                        .anyRequest().authenticated()
                )

                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(accessDeniedHandler)
                        .authenticationEntryPoint(authenticationEntryPoint)

                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2
                                .authenticationEntryPoint(authenticationEntryPoint)
                                .jwt(jwt ->
                                        jwt.jwtAuthenticationConverter(
                                                new JwtAuthenticationConverter()
                                        )
                                )
                );
        return http.build();
    }

}
