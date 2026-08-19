package com.example.ktb_hktn_team2.productkey;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductKeyService {

    private final ProductKeyProperties productKeyProperties;

    /**
     * 제품 키 일치 여부만 판단한다. 저장하거나 토큰을 발급하지 않는다.
     */
    public boolean matches(String key) {
        return productKeyProperties.value().equals(key);
    }
}
