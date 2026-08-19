package com.example.ktb_hktn_team2.productkey;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * app.product-key.value (기본값 9999)
 */
@ConfigurationProperties(prefix = "app.product-key")
public record ProductKeyProperties(String value) {
}
