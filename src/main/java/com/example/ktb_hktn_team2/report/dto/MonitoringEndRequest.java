package com.example.ktb_hktn_team2.report.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class MonitoringEndRequest {

    private LocalDate date; // null이면 오늘 날짜
    private Integer remainingMonitoredSec = 0; // 종료 직전 남아있던 버퍼 초
    private Integer remainingGoodSec = 0;
    private Integer remainingAlerts = 0;
    private String remainingIssues;

    private Integer stretchSuggested = 0;
    private Integer stretchDone = 0;
}
