package com.example.ktb_hktn_team2.auth.dto;

/**
 * 로그인 성공 응답. Access Token 을 response body 로 전달한다.
 *
 * @param accessToken 이후 요청의 {@code Authorization: Bearer <accessToken>} 헤더에 담아 보낸다.
 * @param expiresIn   만료까지 남은 초
 */
public record LoginResponse(String accessToken, String tokenType, long expiresIn, MemberResponse member) {

    private static final String BEARER = "Bearer";

    public static LoginResponse of(String accessToken, long expiresIn, MemberResponse member) {
        return new LoginResponse(accessToken, BEARER, expiresIn, member);
    }
}
