package com.example.ktb_hktn_team2.auth.dto;

import com.example.ktb_hktn_team2.member.Member;

/**
 * 비밀번호는 절대 내려주지 않는다.
 */
public record MemberResponse(Long id, String email) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getEmail());
    }
}
