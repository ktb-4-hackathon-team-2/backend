package com.example.ktb_hktn_team2.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyReportResponse {
    private LocalDate date;
    private Double totalMonitoredMin;
    private Double avgGoodRatio;
    private Integer rate; // 0 ~ 100
    private Integer avgHoldMin; // 시간당 평균 유지(분)
    private Integer totalAlerts;
    private Integer stretchSuggested;
    private Integer stretchDone;
    private String grade; // EXCELLENT, GOOD, NORMAL, BAD
    private String llmSummary;
    private List<String> llmHighlights;
    private List<String> llmAdvice;
    private Boolean hasData; // 실제 모니터링 데이터 유무
    private List<HourlyStatDto> hourlyStats;
}
