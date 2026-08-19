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
public class CalendarDayResponse {
    private LocalDate date;
    private Double totalMonitoredMin;
    private Double avgGoodRatio;
    private Integer rate; // 0 ~ 100
    private Integer level; // 잔디 색상 강도 (0: 없음, 1: 30분 미만, 2: 1시간 미만, 3: 2시간 미만, 4: 2시간 이상)
}
