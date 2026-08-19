package com.example.ktb_hktn_team2.productkey;

import com.example.ktb_hktn_team2.productkey.dto.ProductKeyVerifyRequest;
import com.example.ktb_hktn_team2.productkey.dto.ProductKeyVerifyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product-key")
@RequiredArgsConstructor
public class ProductKeyController {

    private final ProductKeyService productKeyService;

    /**
     * 제품 키 확인. 일치 여부만 내려주며, 프론트는 valid=true 일 때 회원가입/로그인 화면으로 진입시킨다.
     */
    @PostMapping("/verify")
    public ResponseEntity<ProductKeyVerifyResponse> verify(@Valid @RequestBody ProductKeyVerifyRequest request) {
        return ResponseEntity.ok(ProductKeyVerifyResponse.of(productKeyService.matches(request.key())));
    }
}
