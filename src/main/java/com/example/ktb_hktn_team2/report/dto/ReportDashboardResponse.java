package com.example.ktb_hktn_team2.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDashboardResponse {

    // 상단 요약 카드 4개
    private Integer weekAvg; // 이번 주 평균 유지율 %
    private Integer weekDelta; // 지난주 대비 증감 %p
    private Integer avgHold; // 시간당 평균 유지(분)
    private Integer avgHoldDelta; // 지난주 대비 증감(분)
    private Integer streak; // 연속 목표 달성일
    private Integer goal; // 목표 유지율 (기본 70%)
    private Integer todayAlerts; // 오늘 알림 횟수
    private Integer todayAlertDelta; // 어제 대비 알림 증감

    // 차트 데이터
    private List<Days14StatDto> days14; // 최근 14일 일간 추이
    private WeekCompareDto weekThis; // 이번 주 요일별 유지율
    private WeekCompareDto weekLast; // 지난주 요일별 유지율
    private List<HourlyStatDto> hourly; // 시간대별 패턴

    // 코멘트 카드
    private String improvementTitle;
    private String improvementDesc;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeekCompareDto {
        private String label;
        private List<Integer> days; // [월, 화, 수, 목, 금, 토, 일] 7개 값 (미래는 null 또는 0)
    }
}
