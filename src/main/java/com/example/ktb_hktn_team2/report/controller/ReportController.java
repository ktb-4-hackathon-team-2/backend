package com.example.ktb_hktn_team2.report.controller;

import com.example.ktb_hktn_team2.auth.LoginMember;
import com.example.ktb_hktn_team2.member.Member;
import com.example.ktb_hktn_team2.report.dto.*;
import com.example.ktb_hktn_team2.report.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * 1. 프론트엔드 statsReporter.js 1분 집계 전송 (POST /api/monitor/stats)
     */
    @PostMapping("/monitor/stats")
    public ResponseEntity<Map<String, Object>> saveMonitorStats(
            @LoginMember Member member,
            @RequestBody MonitorStatsRequest request
    ) {
        reportService.saveMonitorStats(member, request);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 1-2. 1분 버퍼 로그 저장 (호환용)
     */
    @PostMapping("/monitoring/minute-logs")
    public ResponseEntity<Map<String, Object>> saveMinuteLog(
            @LoginMember Member member,
            @Valid @RequestBody MinuteLogCreateRequest request
    ) {
        reportService.saveMinuteLog(member, request);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 2. 모니터링 종료 (잔여 버퍼 저장 + 당일 집계 + AI 분석 + 일일 레포트 갱신)
     */
    @PostMapping("/monitoring/end")
    public ResponseEntity<DailyReportResponse> endMonitoring(
            @LoginMember Member member,
            @RequestBody(required = false) MonitoringEndRequest request
    ) {
        MonitoringEndRequest req = request != null ? request : new MonitoringEndRequest();
        DailyReportResponse response = reportService.endMonitoringSession(member, req);
        return ResponseEntity.ok(response);
    }

    /**
     * 3. 레포트 화면 전체 대시보드 조회 (최근 14일, 주간비교, 시간대별, 종합스탯)
     */
    @GetMapping("/reports/dashboard")
    public ResponseEntity<ReportDashboardResponse> getDashboard(
            @LoginMember Member member
    ) {
        ReportDashboardResponse dashboard = reportService.getReportDashboard(member);
        return ResponseEntity.ok(dashboard);
    }

    /**
     * 4. 특정 날짜 일일 레포트 상세 조회
     */
    @GetMapping("/reports/daily")
    public ResponseEntity<DailyReportResponse> getDailyReport(
            @LoginMember Member member,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        DailyReportResponse report = reportService.getDailyReport(member, date);
        return ResponseEntity.ok(report);
    }

    /**
     * 5. 달력 잔디(히트맵) 조회
     */
    @GetMapping("/reports/calendar")
    public ResponseEntity<List<CalendarDayResponse>> getCalendar(
            @LoginMember Member member,
            @RequestParam int year,
            @RequestParam int month
    ) {
        List<CalendarDayResponse> calendar = reportService.getCalendarHeatmap(member, year, month);
        return ResponseEntity.ok(calendar);
    }
}
