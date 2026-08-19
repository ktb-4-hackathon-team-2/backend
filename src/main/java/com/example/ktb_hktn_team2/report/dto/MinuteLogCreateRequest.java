package com.example.ktb_hktn_team2.report.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class MinuteLogCreateRequest {

    private LocalDateTime loggedAt; // 생략 시 서버 현재 시간

    @NotNull(message = "monitoredSec는 필수입니다.")
    private Integer monitoredSec; // 1분간 모니터링된 초 (예: 60)

    @NotNull(message = "goodSec는 필수입니다.")
    private Integer goodSec; // 바른 자세 유지 초 (예: 50)

    private Integer alertCount = 0; // 경고 횟수

    private String issueCodes; // "neck_tilt,shoulder_tilt"
}
