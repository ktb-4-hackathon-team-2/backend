package com.example.ktb_hktn_team2.member;

import com.example.ktb_hktn_team2.auth.LoginMember;
import com.example.ktb_hktn_team2.auth.dto.MemberResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MemberController {

    /**
     * 인증이 필요한 API 예시. Authorization 헤더의 Bearer 토큰으로 조회한 회원이 그대로 주입된다.
     * 앞으로 추가할 API 도 파라미터에 {@code @LoginMember Member member} 만 선언하면 된다.
     */
    @GetMapping("/me")
    public ResponseEntity<MemberResponse> me(@LoginMember Member member) {
        return ResponseEntity.ok(MemberResponse.from(member));
    }
}
