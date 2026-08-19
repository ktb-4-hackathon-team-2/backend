package com.example.ktb_hktn_team2.productkey.dto;

public record ProductKeyVerifyResponse(boolean valid) {

    public static ProductKeyVerifyResponse of(boolean valid) {
        return new ProductKeyVerifyResponse(valid);
    }
}
