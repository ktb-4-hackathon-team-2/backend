package com.example.ktb_hktn_team2.settings;

import com.example.ktb_hktn_team2.auth.LoginMember;
import com.example.ktb_hktn_team2.member.Member;
import com.example.ktb_hktn_team2.settings.dto.SettingsRequest;
import com.example.ktb_hktn_team2.settings.dto.SettingsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    /**
     * 저장된 설정 조회. 저장한 적이 없으면 기본값을 내려준다. (항상 200)
     */
    @GetMapping
    public ResponseEntity<SettingsResponse> get(@LoginMember Member member) {
        return ResponseEntity.ok(settingsService.get(member));
    }

    /**
     * 설정 저장(업서트). 4개 필드를 모두 받는다.
     */
    @PutMapping
    public ResponseEntity<SettingsResponse> save(@LoginMember Member member,
                                                 @Valid @RequestBody SettingsRequest request) {
        return ResponseEntity.ok(settingsService.save(member, request));
    }
}
