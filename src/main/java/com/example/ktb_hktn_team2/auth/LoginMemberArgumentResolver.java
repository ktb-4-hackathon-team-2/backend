package com.example.ktb_hktn_team2.auth;

import com.example.ktb_hktn_team2.common.ApiException;
import com.example.ktb_hktn_team2.common.ErrorCode;
import com.example.ktb_hktn_team2.member.Member;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

/**
 * {@link AuthInterceptor} 가 request 에 담아둔 회원을 {@code @LoginMember} 파라미터로 주입한다.
 */
@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMember.class)
                && Member.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        Object member = webRequest.getAttribute(AuthInterceptor.LOGIN_MEMBER_ATTRIBUTE, SCOPE_REQUEST);
        if (member == null) {
            // 인터셉터 제외 경로에 @LoginMember 를 붙인 경우 여기로 온다. (WebConfig 설정 확인 필요)
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        return member;
    }
}
