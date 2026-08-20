package com.example.ktb_hktn_team2.settings;

import java.util.Locale;

/**
 * 알림음. API 로는 소문자(chime, wood, funny, none)로 주고받고, DB 에는 enum 이름(CHIME ...)으로 저장된다.
 * <p>
 * 값을 추가할 때는 {@code SettingsRequest.sound} 의 {@code @Pattern} 정규식도 함께 수정해야 한다.
 */
public enum AlertSound {

    CHIME,
    WOOD,
    FUNNY,
    NONE;

    /**
     * {@code @Pattern} 으로 이미 검증된 값이 들어온다.
     */
    public static AlertSound from(String value) {
        return AlertSound.valueOf(value.toUpperCase(Locale.ROOT));
    }

    /**
     * 프론트로 내려줄 소문자 표기.
     */
    public String value() {
        return name().toLowerCase(Locale.ROOT);
    }
}
