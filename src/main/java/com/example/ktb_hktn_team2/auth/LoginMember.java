package com.example.ktb_hktn_team2.auth;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 인증된 회원을 컨트롤러 파라미터로 주입받는다.
 *
 * <pre>{@code
 * @GetMapping("/api/me")
 * public MemberResponse me(@LoginMember Member member) { ... }
 * }</pre>
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginMember {
}
