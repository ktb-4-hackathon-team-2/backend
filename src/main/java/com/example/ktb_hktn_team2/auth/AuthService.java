package com.example.ktb_hktn_team2.auth;

import com.example.ktb_hktn_team2.auth.dto.LoginRequest;
import com.example.ktb_hktn_team2.auth.dto.LoginResponse;
import com.example.ktb_hktn_team2.auth.dto.MemberResponse;
import com.example.ktb_hktn_team2.auth.dto.SignupRequest;
import com.example.ktb_hktn_team2.common.ApiException;
import com.example.ktb_hktn_team2.common.ErrorCode;
import com.example.ktb_hktn_team2.member.Member;
import com.example.ktb_hktn_team2.member.MemberRepository;
import com.example.ktb_hktn_team2.member.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public MemberResponse signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new ApiException(ErrorCode.DUPLICATE_EMAIL);
        }

        Member member = Member.of(request.email(), passwordEncoder.encode(request.password()));
        return MemberResponse.from(memberRepository.save(member));
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // 이메일이 없는 경우와 비밀번호가 틀린 경우를 구분해서 알려주지 않는다. (계정 존재 여부 노출 방지)
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(ErrorCode.LOGIN_FAILED));

        if (!passwordEncoder.matches(request.password(), member.getPw())) {
            throw new ApiException(ErrorCode.LOGIN_FAILED);
        }

        String accessToken = jwtProvider.createAccessToken(member.getId());
        return LoginResponse.of(accessToken, jwtProvider.getAccessTokenExpiresInSeconds(), MemberResponse.from(member));
    }

    /**
     * Access Token 으로 회원을 조회한다. 인증이 필요한 모든 API 가 이 경로를 탄다.
     */
    @Transactional(readOnly = true)
    public Member findMemberByAccessToken(String accessToken) {
        Long memberId = jwtProvider.parseMemberId(accessToken);
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
