package com.example.ktb_hktn_team2.report.service;

import com.example.ktb_hktn_team2.member.Member;
import com.example.ktb_hktn_team2.report.client.AiClient;
import com.example.ktb_hktn_team2.report.domain.DailyReport;
import com.example.ktb_hktn_team2.report.domain.PostureMinuteLog;
import com.example.ktb_hktn_team2.report.dto.*;
import com.example.ktb_hktn_team2.report.repository.DailyReportRepository;
import com.example.ktb_hktn_team2.report.repository.PostureMinuteLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final PostureMinuteLogRepository minuteLogRepository;
    private final DailyReportRepository dailyReportRepository;
    private final AiClient aiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String[] DOW_KOREAN = {"", "월", "화", "수", "목", "금", "토", "일"};

    /**
     * 1. 프론트엔드 statsReporter.js에서 1분마다 보내는 집계 데이터 저장 (POST /api/monitor/stats)
     */
    @Transactional
    public void saveMonitorStats(Member member, MonitorStatsRequest request) {
        int ticks = request.getTicks() != null ? request.getTicks() : 0;
        // 측정 샘플이 없는 창(전면 일시정지 등)은 저장하지 않는다 — 기본값(60초·100%)으로
        // 채우면 쉬는 시간이 '바른 자세'로 집계돼 유지율·모니터링 시간이 부풀었다.
        if (ticks <= 0 || request.getGoodRatio() == null) return;

        // ticks(2초 판정 샘플 수)에는 일시정지 시간이 이미 빠져 있으므로
        // pausedSec을 다시 빼면 이중 차감이 된다.
        int monitoredSec = ticks * 2;

        double goodRatio = request.getGoodRatio();
        int goodSec = (int) Math.round(monitoredSec * goodRatio);
        int alerts = request.getAlerts() != null ? request.getAlerts() : 0;

        // 발생 횟수를 보존해 "code:count" 형태로 저장 — 원인 분석이 '등장한 분(minute) 수'가
        // 아니라 실제 발생 횟수를 세도록 (calculateIssueStats가 구형 "code" 형태도 함께 파싱)
        String issueCodes = "";
        if (request.getIssueCounts() != null && !request.getIssueCounts().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Integer> en : request.getIssueCounts().entrySet()) {
                if (sb.length() > 0) sb.append(',');
                sb.append(en.getKey()).append(':').append(en.getValue() != null ? en.getValue() : 1);
            }
            issueCodes = sb.toString();
        }

        LocalDateTime loggedAt = LocalDateTime.now();
        if (request.getWindowStart() != null && !request.getWindowStart().isBlank()) {
            try {
                loggedAt = LocalDateTime.ofInstant(Instant.parse(request.getWindowStart()), ZoneId.systemDefault());
            } catch (Exception e) {
                loggedAt = LocalDateTime.now();
            }
        }

        PostureMinuteLog logEntity = PostureMinuteLog.builder()
                .member(member)
                .loggedAt(loggedAt)
                .monitoredSec(monitoredSec)
                .goodSec(goodSec)
                .alertCount(alerts)
                .issueCodes(issueCodes)
                .build();
        minuteLogRepository.save(logEntity);
    }

    /**
     * 기존 1분 버퍼링 수동 전송용 (호환 유지)
     */
    @Transactional
    public void saveMinuteLog(Member member, MinuteLogCreateRequest request) {
        PostureMinuteLog logEntity = PostureMinuteLog.builder()
                .member(member)
                .loggedAt(request.getLoggedAt() != null ? request.getLoggedAt() : LocalDateTime.now())
                .monitoredSec(request.getMonitoredSec())
                .goodSec(request.getGoodSec())
                .alertCount(request.getAlertCount())
                .issueCodes(request.getIssueCodes())
                .build();
        minuteLogRepository.save(logEntity);
    }

    /**
     * 2. 모니터링 종료 시: 남은 버퍼 저장 -> 당일 집계 -> AI 분석 -> DailyReport 저장 -> 결과 반환
     */
    @Transactional
    public DailyReportResponse endMonitoringSession(Member member, MonitoringEndRequest request) {
        LocalDate targetDate = request.getDate() != null ? request.getDate() : LocalDate.now();

        // 1) 종료 직전 잔여 버퍼 저장
        if (request.getRemainingMonitoredSec() != null && request.getRemainingMonitoredSec() > 0) {
            minuteLogRepository.save(PostureMinuteLog.builder()
                    .member(member)
                    .loggedAt(LocalDateTime.now())
                    .monitoredSec(request.getRemainingMonitoredSec())
                    .goodSec(request.getRemainingGoodSec())
                    .alertCount(request.getRemainingAlerts())
                    .issueCodes(request.getRemainingIssues())
                    .build());
        }

        // 2) 오늘 날짜 로그 집계
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);

        List<Object[]> summaryList = minuteLogRepository.findSummaryBetween(member, startOfDay, endOfDay);
        long totalMonitoredSec = 0;
        long totalGoodSec = 0;
        int totalAlerts = 0;

        if (!summaryList.isEmpty() && summaryList.get(0) != null) {
            Object[] row = summaryList.get(0);
            totalMonitoredSec = ((Number) row[0]).longValue();
            totalGoodSec = ((Number) row[1]).longValue();
            totalAlerts = ((Number) row[2]).intValue();
        }

        double totalMonitoredMin = Math.round((totalMonitoredSec / 60.0) * 10.0) / 10.0;
        double avgGoodRatio = totalMonitoredSec > 0
                ? Math.round(((double) totalGoodSec / totalMonitoredSec) * 100.0) / 100.0
                : 0.0;
        int rate = (int) Math.round(avgGoodRatio * 100);
        int avgHoldMin = (int) Math.round(avgGoodRatio * 60);

        List<HourlyStatDto> hourlyStats = getEnrichedHourlyStats(member, targetDate);
        List<HourlyStatDto> yesterdayHourlyStats = getEnrichedHourlyStats(member, targetDate.minusDays(1));
        List<IssueStatDto> issueStats = calculateIssueStats(member, startOfDay, endOfDay);

        // 3) AI 서버 분석 호출 — 종료 시 자동 호출이므로 쿨다운을 적용한다(force=false)
        AiClient.AiAnalyzeResult aiResult = aiClient.analyzeDaily(
                String.valueOf(member.getId()),
                targetDate.toString(),
                hourlyStats,
                request.getStretchSuggested(),
                request.getStretchDone(),
                false
        );

        // 4) DailyReport 테이블에 저장(UPSERT)
        String highlightsJson = toJson(aiResult.highlights());
        String adviceJson = toJson(aiResult.advice());

        DailyReport dailyReport = dailyReportRepository.findByMemberAndReportDate(member, targetDate)
                .orElseGet(() -> DailyReport.builder()
                        .member(member)
                        .reportDate(targetDate)
                        .build());

        dailyReport.updateAnalysis(
                totalMonitoredMin,
                avgGoodRatio,
                totalAlerts,
                request.getStretchSuggested(),
                request.getStretchDone(),
                aiResult.grade(),
                aiResult.summary(),
                highlightsJson,
                adviceJson,
                aiResult.source()
        );
        dailyReportRepository.save(dailyReport);

        return DailyReportResponse.builder()
                .date(targetDate)
                .totalMonitoredMin(totalMonitoredMin)
                .avgGoodRatio(avgGoodRatio)
                .rate(rate)
                .avgHoldMin(avgHoldMin)
                .totalAlerts(totalAlerts)
                .stretchSuggested(request.getStretchSuggested())
                .stretchDone(request.getStretchDone())
                .grade(aiResult.grade())
                .llmSummary(aiResult.summary())
                .llmHighlights(aiResult.highlights())
                .llmAdvice(aiResult.advice())
                .hasData(true)
                .hourlyStats(hourlyStats)
                .yesterdayHourlyStats(yesterdayHourlyStats)
                .issueStats(issueStats)
                .build();
    }

    /**
     * 2-2. 버튼 클릭 시 AI 리포트 분석 생성 / 재생성
     */
    @Transactional
    public DailyReportResponse generateAiReport(Member member, LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);

        List<Object[]> summaryList = minuteLogRepository.findSummaryBetween(member, startOfDay, endOfDay);
        long totalMonitoredSec = 0;
        long totalGoodSec = 0;
        int totalAlerts = 0;

        if (!summaryList.isEmpty() && summaryList.get(0) != null) {
            Object[] row = summaryList.get(0);
            totalMonitoredSec = ((Number) row[0]).longValue();
            totalGoodSec = ((Number) row[1]).longValue();
            totalAlerts = ((Number) row[2]).intValue();
        }

        double totalMonitoredMin = Math.round((totalMonitoredSec / 60.0) * 10.0) / 10.0;
        double avgGoodRatio = totalMonitoredSec > 0
                ? Math.round(((double) totalGoodSec / totalMonitoredSec) * 100.0) / 100.0
                : 0.0;
        int rate = (int) Math.round(avgGoodRatio * 100);
        int avgHoldMin = (int) Math.round(avgGoodRatio * 60);

        List<HourlyStatDto> hourlyStats = getEnrichedHourlyStats(member, targetDate);
        List<HourlyStatDto> yesterdayHourlyStats = getEnrichedHourlyStats(member, targetDate.minusDays(1));
        List<IssueStatDto> issueStats = calculateIssueStats(member, startOfDay, endOfDay);

        // AI 서버 분석 호출 — 사용자가 '재생성'을 직접 누른 경로이므로 쿨다운을 건너뛴다(force=true)
        AiClient.AiAnalyzeResult aiResult = aiClient.analyzeDaily(
                String.valueOf(member.getId()),
                targetDate.toString(),
                hourlyStats,
                0,
                0,
                true
        );

        // DailyReport 테이블 저장(UPSERT)
        String highlightsJson = toJson(aiResult.highlights());
        String adviceJson = toJson(aiResult.advice());

        DailyReport dailyReport = dailyReportRepository.findByMemberAndReportDate(member, targetDate)
                .orElseGet(() -> DailyReport.builder()
                        .member(member)
                        .reportDate(targetDate)
                        .build());

        dailyReport.updateAnalysis(
                totalMonitoredMin,
                avgGoodRatio,
                totalAlerts,
                0,
                0,
                aiResult.grade(),
                aiResult.summary(),
                highlightsJson,
                adviceJson,
                aiResult.source()
        );
        dailyReportRepository.save(dailyReport);

        return DailyReportResponse.builder()
                .date(targetDate)
                .totalMonitoredMin(totalMonitoredMin)
                .avgGoodRatio(avgGoodRatio)
                .rate(rate)
                .avgHoldMin(avgHoldMin)
                .totalAlerts(totalAlerts)
                .stretchSuggested(0)
                .stretchDone(0)
                .grade(aiResult.grade())
                .llmSummary(aiResult.summary())
                .llmHighlights(aiResult.highlights())
                .llmAdvice(aiResult.advice())
                .hasData(totalMonitoredSec > 0)
                .hourlyStats(hourlyStats)
                .yesterdayHourlyStats(yesterdayHourlyStats)
                .issueStats(issueStats)
                .build();
    }

    /**
     * 3. 특정 날짜 일일 레포트 조회
     */
    public DailyReportResponse getDailyReport(Member member, LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);

        List<HourlyStatDto> hourlyStats = getEnrichedHourlyStats(member, targetDate);
        List<HourlyStatDto> yesterdayHourlyStats = getEnrichedHourlyStats(member, targetDate.minusDays(1));
        List<IssueStatDto> issueStats = calculateIssueStats(member, startOfDay, endOfDay);

        Optional<DailyReport> dailyReportOpt = dailyReportRepository.findByMemberAndReportDate(member, targetDate);

        if (dailyReportOpt.isPresent()) {
            DailyReport r = dailyReportOpt.get();
            boolean hasData = r.getTotalMonitoredMin() != null && r.getTotalMonitoredMin() > 0;
            return DailyReportResponse.builder()
                    .date(r.getReportDate())
                    .totalMonitoredMin(r.getTotalMonitoredMin())
                    .avgGoodRatio(r.getAvgGoodRatio())
                    .rate(hasData ? (int) Math.round(r.getAvgGoodRatio() * 100) : null)
                    .avgHoldMin(hasData ? (int) Math.round(r.getAvgGoodRatio() * 60) : null)
                    .totalAlerts(r.getTotalAlerts())
                    .stretchSuggested(r.getStretchSuggested())
                    .stretchDone(r.getStretchDone())
                    .grade(r.getGrade())
                    .llmSummary(r.getLlmSummary())
                    .llmHighlights(fromJsonList(r.getLlmHighlights()))
                    .llmAdvice(fromJsonList(r.getLlmAdvice()))
                    .hasData(hasData)
                    .hourlyStats(hourlyStats)
                    .yesterdayHourlyStats(yesterdayHourlyStats)
                    .issueStats(issueStats)
                    .build();
        }

        // DB에 DailyReport 레코드가 아직 없는 경우 로그 기반 즉석 계산
        List<Object[]> summaryList = minuteLogRepository.findSummaryBetween(member, startOfDay, endOfDay);
        long totalSec = 0, goodSec = 0;
        int alerts = 0;
        if (!summaryList.isEmpty() && summaryList.get(0) != null) {
            Object[] row = summaryList.get(0);
            totalSec = ((Number) row[0]).longValue();
            goodSec = ((Number) row[1]).longValue();
            alerts = ((Number) row[2]).intValue();
        }
        boolean hasData = totalSec > 0;
        double totalMin = Math.round((totalSec / 60.0) * 10.0) / 10.0;
        double goodRatio = hasData ? Math.round(((double) goodSec / totalSec) * 100.0) / 100.0 : 0.0;

        return DailyReportResponse.builder()
                .date(targetDate)
                .totalMonitoredMin(totalMin)
                .avgGoodRatio(goodRatio)
                .rate(hasData ? (int) Math.round(goodRatio * 100) : null)
                .avgHoldMin(hasData ? (int) Math.round(goodRatio * 60) : null)
                .totalAlerts(alerts)
                .stretchSuggested(0)
                .stretchDone(0)
                .grade(hasData ? "NORMAL" : null)
                .llmSummary(hasData ? "오늘 모니터링을 진행해 보세요." : "이 날은 모니터링 기록이 없어요.")
                .llmHighlights(List.of())
                .llmAdvice(List.of())
                .hasData(hasData)
                .hourlyStats(hourlyStats)
                .yesterdayHourlyStats(yesterdayHourlyStats)
                .issueStats(issueStats)
                .build();
    }

    private List<HourlyStatDto> getEnrichedHourlyStats(Member member, LocalDate targetDate) {
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);
        List<HourlyStatDto> hourlyStats = minuteLogRepository.findHourlyStatsBetween(member, startOfDay, endOfDay);
        if (hourlyStats != null && !hourlyStats.isEmpty()) {
            for (HourlyStatDto h : hourlyStats) {
                if (h.getHour() != null) {
                    LocalDateTime hStart = targetDate.atTime(h.getHour(), 0, 0, 0);
                    LocalDateTime hEnd = targetDate.atTime(h.getHour(), 59, 59, 999999999);
                    h.setIssueStats(calculateIssueStats(member, hStart, hEnd));
                }
            }
        }
        return hourlyStats != null ? hourlyStats : List.of();
    }

    /**
     * 4. 프론트엔드 Report.jsx 대시보드 전체 종합 조회 (최근 14일, 주간 비교, 시간대별, 스탯 카드)
     */
    public ReportDashboardResponse getReportDashboard(Member member) {
        LocalDate today = LocalDate.now();

        // 1) 최근 14일 일자별 통계 (DAYS14)
        List<Days14StatDto> days14List = new ArrayList<>();
        int streak = 0;
        int goal = 70; // 목표 70%

        for (int i = 13; i >= 0; i--) {
            LocalDate target = today.minusDays(i);
            LocalDateTime s = target.atStartOfDay();
            LocalDateTime e = target.atTime(LocalTime.MAX);

            List<Object[]> summary = minuteLogRepository.findSummaryBetween(member, s, e);
            long totalSec = 0, goodSec = 0;
            int alerts = 0;
            if (!summary.isEmpty() && summary.get(0) != null) {
                totalSec = ((Number) summary.get(0)[0]).longValue();
                goodSec = ((Number) summary.get(0)[1]).longValue();
                alerts = ((Number) summary.get(0)[2]).intValue();
            }

            boolean hasData = totalSec > 0;
            double goodRatio = hasData ? ((double) goodSec / totalSec) : 0.0;
            Integer rate = hasData ? (int) Math.round(goodRatio * 100) : null;
            Integer holdMin = hasData ? (int) Math.round(goodRatio * 60) : null;
            double totalMin = Math.round((totalSec / 60.0) * 10.0) / 10.0;

            String d = target.getMonthValue() + "/" + target.getDayOfMonth();
            String dow = DOW_KOREAN[target.getDayOfWeek().getValue()];

            days14List.add(Days14StatDto.builder()
                    .d(d)
                    .dow(dow)
                    .rate(rate)
                    .hold(holdMin)
                    .totalMin(totalMin)
                    .alertCount(alerts)
                    .hasData(hasData)
                    .today(target.isEqual(today))
                    .fullDate(target)
                    .build());

            if (hasData && rate != null && rate >= goal) {
                streak++;
            } else if (!target.isEqual(today)) {
                streak = 0; // 연속 리셋
            }
        }

        // 2) 주간 비교 (이번 주 vs 지난주)
        LocalDate thisWeekMonday = today.with(DayOfWeek.MONDAY);
        LocalDate lastWeekMonday = thisWeekMonday.minusWeeks(1);

        List<Integer> thisWeekDays = new ArrayList<>();
        List<Integer> lastWeekDays = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate thisDate = thisWeekMonday.plusDays(i);
            if (thisDate.isAfter(today)) {
                // 미래 날짜는 null 또는 0
                thisWeekDays.add(null);
            } else {
                thisWeekDays.add(calculateDayRate(member, thisDate));
            }

            LocalDate lastDate = lastWeekMonday.plusDays(i);
            lastWeekDays.add(calculateDayRate(member, lastDate));
        }

        double thisWeekAvg = thisWeekDays.stream()
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        double lastWeekAvg = lastWeekDays.stream()
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        int weekAvgInt = (int) Math.round(thisWeekAvg);
        int weekDelta = (int) Math.round(thisWeekAvg - lastWeekAvg);
        int avgHold = (int) Math.round((thisWeekAvg / 100.0) * 60);

        // 3) 오늘 알림 및 시간대별 패턴
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
        LocalDateTime yesterdayStart = today.minusDays(1).atStartOfDay();
        LocalDateTime yesterdayEnd = today.minusDays(1).atTime(LocalTime.MAX);

        int todayAlerts = getAlertCount(member, todayStart, todayEnd);
        int yesterdayAlerts = getAlertCount(member, yesterdayStart, yesterdayEnd);
        int alertDelta = todayAlerts - yesterdayAlerts;

        List<HourlyStatDto> hourly = minuteLogRepository.findHourlyStatsBetween(member, todayStart, todayEnd);

        return ReportDashboardResponse.builder()
                .weekAvg(weekAvgInt)
                .weekDelta(weekDelta)
                .avgHold(avgHold)
                .avgHoldDelta(0)
                .streak(streak)
                .goal(goal)
                .todayAlerts(todayAlerts)
                .todayAlertDelta(alertDelta)
                .days14(days14List)
                .weekThis(new ReportDashboardResponse.WeekCompareDto("이번 주", thisWeekDays))
                .weekLast(new ReportDashboardResponse.WeekCompareDto("지난주", lastWeekDays))
                .hourly(hourly)
                .improvementTitle("자세 개선 추이")
                .improvementDesc("매일 꾸준한 모니터링으로 바른 자세 습관을 만들어가고 있어요.")
                .build();
    }

    /**
     * 5. 달력 잔디(히트맵) 조회
     */
    public List<CalendarDayResponse> getCalendarHeatmap(Member member, int year, int month) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);

        List<CalendarDayResponse> result = new ArrayList<>();

        for (LocalDate date = startOfMonth; !date.isAfter(endOfMonth); date = date.plusDays(1)) {
            LocalDateTime s = date.atStartOfDay();
            LocalDateTime e = date.atTime(LocalTime.MAX);

            List<Object[]> summary = minuteLogRepository.findSummaryBetween(member, s, e);
            long totalSec = 0, goodSec = 0;
            if (!summary.isEmpty() && summary.get(0) != null) {
                totalSec = ((Number) summary.get(0)[0]).longValue();
                goodSec = ((Number) summary.get(0)[1]).longValue();
            }

            double totalMin = Math.round((totalSec / 60.0) * 10.0) / 10.0;
            double goodRatio = totalSec > 0 ? Math.round(((double) goodSec / totalSec) * 100.0) / 100.0 : 0.0;
            int rate = (int) Math.round(goodRatio * 100);

            // 잔디 색상 단계 (0~4)
            int level = 0;
            if (totalMin >= 120) level = 4;
            else if (totalMin >= 60) level = 3;
            else if (totalMin >= 30) level = 2;
            else if (totalMin > 0) level = 1;

            result.add(CalendarDayResponse.builder()
                    .date(date)
                    .totalMonitoredMin(totalMin)
                    .avgGoodRatio(goodRatio)
                    .rate(rate)
                    .level(level)
                    .build());
        }

        return result;
    }

    private int calculateDayRate(Member member, LocalDate date) {
        LocalDateTime s = date.atStartOfDay();
        LocalDateTime e = date.atTime(LocalTime.MAX);
        List<Object[]> summary = minuteLogRepository.findSummaryBetween(member, s, e);
        if (!summary.isEmpty() && summary.get(0) != null) {
            long totalSec = ((Number) summary.get(0)[0]).longValue();
            long goodSec = ((Number) summary.get(0)[1]).longValue();
            if (totalSec > 0) {
                return (int) Math.round(((double) goodSec / totalSec) * 100);
            }
        }
        return 0;
    }

    private int getAlertCount(Member member, LocalDateTime start, LocalDateTime end) {
        List<Object[]> summary = minuteLogRepository.findSummaryBetween(member, start, end);
        if (!summary.isEmpty() && summary.get(0) != null) {
            return ((Number) summary.get(0)[2]).intValue();
        }
        return 0;
    }

    private List<IssueStatDto> calculateIssueStats(Member member, LocalDateTime start, LocalDateTime end) {
        List<String> rawIssueCodesList = minuteLogRepository.findIssueCodesBetween(member, start, end);
        if (rawIssueCodesList == null || rawIssueCodesList.isEmpty()) {
            return List.of();
        }

        Map<String, Integer> counts = new HashMap<>();
        int totalOccurrences = 0;
        for (String raw : rawIssueCodesList) {
            if (raw == null || raw.isBlank()) continue;
            String[] split = raw.split(",");
            for (String code : split) {
                String trimmed = code.trim();
                if (trimmed.isEmpty()) continue;
                // 신형 "code:count"와 구형 "code"(횟수 1로 간주)를 모두 지원
                int count = 1;
                int sep = trimmed.indexOf(':');
                if (sep > 0) {
                    try {
                        count = Math.max(1, Integer.parseInt(trimmed.substring(sep + 1).trim()));
                    } catch (NumberFormatException ignored) {
                        count = 1;
                    }
                    trimmed = trimmed.substring(0, sep).trim();
                    if (trimmed.isEmpty()) continue;
                }
                counts.merge(trimmed, count, Integer::sum);
                totalOccurrences += count;
            }
        }

        if (totalOccurrences == 0) {
            return List.of();
        }

        final int total = totalOccurrences;
        List<IssueStatDto> stats = new ArrayList<>();
        counts.forEach((code, count) -> {
            int ratio = (int) Math.round(((double) count / total) * 100);
            stats.add(toIssueStatDto(code, count, ratio));
        });

        stats.sort((a, b) -> b.getCount().compareTo(a.getCount()));
        return stats;
    }

    private IssueStatDto toIssueStatDto(String code, int count, int ratio) {
        String label;
        String desc;
        String stretch;
        String stretchId;

        switch (code) {
            case "neck_tilt":
                label = "거북목 · 목 기울임";
                desc = "머리가 화면 쪽으로 앞으로 쏠리거나 목이 한쪽으로 기운 상태";
                stretch = "턱 당기기";
                stretchId = "chin_tuck";
                break;
            case "shoulder_tilt":
                label = "어깨 비대칭 · 기울어짐";
                desc = "양쪽 어깨의 높낮이가 맞지 않고 한쪽으로 치우친 상태";
                stretch = "어깨 으쓱하기";
                stretchId = "shoulder_shrug";
                break;
            case "head_down":
                label = "고개 숙임";
                desc = "시선이 모니터보다 지나치게 낮아 목 뒤 근육에 부담이 가는 상태";
                stretch = "가슴 열기 (양팔 벌리기)";
                stretchId = "chest_opener";
                break;
            case "lean_in":
                label = "화면 밀착";
                desc = "모니터 화면에 너무 가까이 다가가 눈과 목에 무리가 가는 상태";
                stretch = "팔 위로 뻗기";
                stretchId = "arms_up";
                break;
            case "lean_out":
                label = "뒤로 눕는 자세";
                desc = "의자 등받이에 과도하게 기대어 척추 정렬이 무너진 상태";
                stretch = "가슴 열기";
                stretchId = "chest_opener";
                break;
            case "shift_x":
                label = "좌우 쏠림";
                desc = "상체가 중심선에서 좌우로 크게 벗어난 상태";
                stretch = "팔 위로 뻗기";
                stretchId = "arms_up";
                break;
            default:
                label = code;
                desc = "자세 불균형이 감지된 상태";
                stretch = "목 옆 늘리기";
                stretchId = "neck_side_left";
                break;
        }

        return IssueStatDto.builder()
                .code(code)
                .label(label)
                .count(count)
                .ratio(ratio)
                .description(desc)
                .recommendedStretch(stretch)
                .stretchId(stretchId)
                .build();
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> fromJsonList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            return List.of();
        }
    }
}
