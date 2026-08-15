package com.sajee.auth.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;

import java.security.interfaces.RSAPublicKey;

@Configuration
@RequiredArgsConstructor
public class JwtDecoderConfiguration {

    private final JwtProperties jwtProperties;

    // Decode JWT, verify signature, and validate issuer/audience
    @Bean
    public JwtDecoder jwtDecoder(RSAPublicKey publicKey) {

        // Decode & verify signature
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withPublicKey(publicKey)
                .build();

        // Accept the JWT only if 'iss' claim matches 'jwtProperties.issuer()'
        // Not only validate iss, includes the default validators, such as exp, nbf, etc.
        OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators
                .createDefaultWithIssuer(jwtProperties.issuer());

        // Accept the JWT only if 'aud' claim matches 'jwtProperties.audience()'
        OAuth2TokenValidator<Jwt> audienceValidator =
                new JwtAudienceValidator(jwtProperties.audience());

        // Combines validators.
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                issuerValidator,
                audienceValidator
        );

        decoder.setJwtValidator(validator);
        return decoder;
    }
}
