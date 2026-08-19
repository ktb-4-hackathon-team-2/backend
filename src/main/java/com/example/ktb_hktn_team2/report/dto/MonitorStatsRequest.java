package com.example.ktb_hktn_team2.report.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
public class MonitorStatsRequest {

    @JsonProperty("window_start")
    private String windowStart; // ISO8601 (예: 2026-08-19T12:15:00.000Z)

    @JsonProperty("window_end")
    private String windowEnd;

    private Integer ticks; // 판정 횟수 (2초에 1틱, 1분에 약 30회)

    @JsonProperty("good_ratio")
    private Double goodRatio; // 바른 자세 비율 0~1

    @JsonProperty("avg_score")
    private Double avgScore; // 평균 편차 점수

    private Integer alerts; // 경고(레벨 1+) 발생 횟수

    @JsonProperty("issue_counts")
    private Map<String, Integer> issueCounts; // { "neck_tilt": 2, "shoulder_tilt": 1 }

    @JsonProperty("paused_sec")
    private Integer pausedSec; // 일시정지 시간 (초)
}
