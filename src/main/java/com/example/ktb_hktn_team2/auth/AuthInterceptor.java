package com.example.ktb_hktn_team2.auth;

import com.example.ktb_hktn_team2.common.ApiException;
import com.example.ktb_hktn_team2.common.ErrorCode;
import com.example.ktb_hktn_team2.member.Member;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Authorization: Bearer &lt;token&gt; 헤더에서 토큰을 꺼내 검증하고, 회원 DB 에서 조회해 request 에 담아둔다.
 * 실제 주입은 {@link LoginMemberArgumentResolver} 가 담당한다.
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    static final String LOGIN_MEMBER_ATTRIBUTE = "loginMember";

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 정적 리소스나 CORS preflight 요청은 그냥 통과시킨다.
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        Member member = authService.findMemberByAccessToken(extractAccessToken(request));
        request.setAttribute(LOGIN_MEMBER_ATTRIBUTE, member);
        return true;
    }

    private String extractAccessToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(header) || !header.startsWith(BEARER_PREFIX)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();
        if (!StringUtils.hasText(token)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        return token;
    }
}
