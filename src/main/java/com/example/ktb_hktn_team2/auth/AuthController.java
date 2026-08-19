package com.example.ktb_hktn_team2.auth;

import com.example.ktb_hktn_team2.auth.dto.LoginRequest;
import com.example.ktb_hktn_team2.auth.dto.LoginResponse;
import com.example.ktb_hktn_team2.auth.dto.MemberResponse;
import com.example.ktb_hktn_team2.auth.dto.SignupRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<MemberResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
    }

    /**
     * 로그인 성공 시 Access Token 을 response body 로 전달한다.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
