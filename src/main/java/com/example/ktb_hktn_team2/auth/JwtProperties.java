package com.example.ktb_hktn_team2.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * app.jwt.secret / app.jwt.access-token-expiration
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, Duration accessTokenExpiration) {
}
