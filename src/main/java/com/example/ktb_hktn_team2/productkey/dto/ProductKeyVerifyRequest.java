package com.example.ktb_hktn_team2.productkey.dto;

import jakarta.validation.constraints.NotBlank;

public record ProductKeyVerifyRequest(

        @NotBlank(message = "제품 키를 입력해주세요.")
        String key
) {
}
