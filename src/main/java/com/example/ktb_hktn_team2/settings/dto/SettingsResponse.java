package com.example.ktb_hktn_team2.settings.dto;

import com.example.ktb_hktn_team2.settings.MemberSettings;

/**
 * 프론트 계약상 이 4개 필드만 내려간다. (updatedAt 등 추가 금지)
 */
public record SettingsResponse(int sensitivity, String sound, int maxAlertLevel, int stretchMin) {

    public static SettingsResponse from(MemberSettings settings) {
        return new SettingsResponse(
                settings.getSensitivity(),
                settings.getSound().value(),
                settings.getMaxAlertLevel(),
                settings.getStretchMin()
        );
    }

    /**
     * 아직 저장한 적 없는 회원에게 내려줄 기본 설정. 기본값 정의는 {@link MemberSettings} 의 상수가 유일한 출처다.
     */
    public static SettingsResponse defaults() {
        return new SettingsResponse(
                MemberSettings.DEFAULT_SENSITIVITY,
                MemberSettings.DEFAULT_SOUND.value(),
                MemberSettings.DEFAULT_MAX_ALERT_LEVEL,
                MemberSettings.DEFAULT_STRETCH_MIN
        );
    }
}
