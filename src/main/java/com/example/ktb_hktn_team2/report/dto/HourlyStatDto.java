package com.example.ktb_hktn_team2.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HourlyStatDto {
    private Integer hour; // 0 ~ 23
    private String h; // "9시", "10시" (프론트 차트 호환)
    private Double goodRatio; // 0.0 ~ 1.0
    private Integer rate; // 0 ~ 100 (프론트 차트 호환 정수 %)
    private Double monitoredMin; // 모니터링 시간(분)
    private Integer alerts; // 경고 횟수
    private List<IssueStatDto> issueStats; // 해당 시간대 세부 원인 분석

    public HourlyStatDto(Integer hour, Double goodRatio, Double monitoredMin, Long alerts) {
        this.hour = hour;
        this.h = (hour != null ? hour : 0) + "시";
        this.goodRatio = goodRatio != null ? Math.round(goodRatio * 100.0) / 100.0 : 0.0;
        this.rate = this.goodRatio != null ? (int) Math.round(this.goodRatio * 100) : 0;
        this.monitoredMin = monitoredMin != null ? Math.round(monitoredMin * 10.0) / 10.0 : 0.0;
        this.alerts = alerts != null ? alerts.intValue() : 0;
        this.issueStats = List.of();
    }
}
