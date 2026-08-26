package com.sajee.auth.security.cors;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "secureid.cors")
public record CorsProperties(
    String  allowedOrigin
){
}
