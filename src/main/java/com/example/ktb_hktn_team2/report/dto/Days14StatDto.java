package com.example.ktb_hktn_team2.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Days14StatDto {
    private String d; // "8/19"
    private String dow; // "수"
    private Integer rate; // 0 ~ 100 (데이터 없으면 null)
    private Integer hold; // 시간당 유지 (분)
    private Double totalMin; // 총 모니터링 시간(분)
    private Integer alertCount; // 경고 알림 수
    private Boolean hasData; // 실제 측정 데이터가 있는지 여부
    private Boolean today; // 오늘 여부
    private LocalDate fullDate; // 2026-08-19
}
