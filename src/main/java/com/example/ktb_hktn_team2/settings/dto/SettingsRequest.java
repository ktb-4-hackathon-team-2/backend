package com.example.ktb_hktn_team2.settings.dto;

import com.example.ktb_hktn_team2.common.validation.MultipleOf;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 프론트가 항상 4개 필드를 모두 보낸다. (부분 업데이트 없음)
 * <p>
 * int 가 아니라 Integer 인 이유: int 면 필드를 빼먹었을 때 Jackson 이 0 으로 채워버려서
 * 누락과 "0을 보낸 것"을 구분할 수 없다. Integer + @NotNull 이어야 400 으로 걸러진다.
 */
public record SettingsRequest(

        @NotNull(message = "sensitivity 는 필수입니다.")
        @Min(value = 0, message = "sensitivity 는 0 이상이어야 합니다.")
        @Max(value = 100, message = "sensitivity 는 100 이하여야 합니다.")
        Integer sensitivity,

        @NotBlank(message = "sound 는 필수입니다.")
        @Pattern(regexp = "chime|wood|funny|none", message = "sound 는 chime, wood, funny, none 중 하나여야 합니다.")
        String sound,

        @NotNull(message = "maxAlertLevel 은 필수입니다.")
        @Min(value = 1, message = "maxAlertLevel 은 1 이상이어야 합니다.")
        @Max(value = 3, message = "maxAlertLevel 은 3 이하여야 합니다.")
        Integer maxAlertLevel,

        @NotNull(message = "stretchMin 은 필수입니다.")
        @Min(value = 30, message = "stretchMin 은 30 이상이어야 합니다.")
        @Max(value = 90, message = "stretchMin 은 90 이하여야 합니다.")
        @MultipleOf(value = 10, message = "stretchMin 은 10분 단위여야 합니다. (30, 40, 50, 60, 70, 80, 90)")
        Integer stretchMin
) {
}
