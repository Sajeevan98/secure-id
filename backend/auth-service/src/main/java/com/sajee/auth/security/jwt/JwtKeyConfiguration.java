package com.sajee.auth.security.jwt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class JwtKeyConfiguration {

    @Bean
    public KeyPair jwtKeyPair() {

        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

            generator.initialize(2048);
            return generator.generateKeyPair();

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("RSA algorithm is not available", exception);
        }
    }

    @Bean
    public RSAPrivateKey jwtPrivateKey(KeyPair jwtKeyPair) {
        return (RSAPrivateKey) jwtKeyPair.getPrivate();
    }

    @Bean
    public RSAPublicKey jwtPublicKey(KeyPair jwtKeyPair) {
        return (RSAPublicKey) jwtKeyPair.getPublic();
    }

    // JWT-claims into Signed-JWT(header.payload.signature)
    @Bean
    public JwtEncoder jwtEncoder(RSAPublicKey publicKey, RSAPrivateKey privateKey) {

        return NimbusJwtEncoder
                .withKeyPair(publicKey, privateKey)
                .build();
    }

    // decode: sub, aud, iss, exp, iat, jti, username
    // But verify only signature. Not iss, aud.
    // If it has a valid signature made with corresponding private key, the decoder can successfully decode it.
    // So, Create separate configuration for 'JwtDecoderConfiguration'.

//    @Bean
//    public JwtDecoder jwtDecoder(RSAPublicKey publicKey) {
//
//        return NimbusJwtDecoder
//                .withPublicKey(publicKey)
//                .build();
//    }
}
