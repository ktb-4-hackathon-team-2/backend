package com.example.ktb_hktn_team2.settings;

import com.example.ktb_hktn_team2.member.Member;
import com.example.ktb_hktn_team2.settings.dto.SettingsRequest;
import com.example.ktb_hktn_team2.settings.dto.SettingsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final MemberSettingsRepository memberSettingsRepository;

    /**
     * 저장한 적이 없으면 기본값을 내려준다. (행을 만들지는 않는다 — 실제 저장은 PUT 에서만)
     */
    @Transactional(readOnly = true)
    public SettingsResponse get(Member member) {
        return memberSettingsRepository.findById(member.getId())
                .map(SettingsResponse::from)
                .orElseGet(SettingsResponse::defaults);
    }

    /**
     * 업서트. 회원당 1행이라 있으면 갱신, 없으면 기본값 행을 만든 뒤 요청값으로 덮어쓴다.
     */
    @Transactional
    public SettingsResponse save(Member member, SettingsRequest request) {
        MemberSettings settings = memberSettingsRepository.findById(member.getId())
                .orElseGet(() -> memberSettingsRepository.save(MemberSettings.defaultsOf(member)));

        settings.update(request.sensitivity(), AlertSound.from(request.sound()),
                request.maxAlertLevel(), request.stretchMin());

        return SettingsResponse.from(settings);
    }
}
