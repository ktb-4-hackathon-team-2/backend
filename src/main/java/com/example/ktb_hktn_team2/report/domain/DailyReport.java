package com.example.ktb_hktn_team2.report.domain;

import com.example.ktb_hktn_team2.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "daily_report",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_member_report_date", columnNames = {"member_id", "report_date"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "total_monitored_min", nullable = false)
    private Double totalMonitoredMin; // 하루 총 모니터링 시간(분)

    @Column(name = "avg_good_ratio", nullable = false)
    private Double avgGoodRatio; // 하루 평균 바른자세율 (0.0 ~ 1.0)

    @Column(name = "total_alerts", nullable = false)
    private Integer totalAlerts; // 총 경고 수

    @Column(name = "stretch_suggested", nullable = false)
    private Integer stretchSuggested;

    @Column(name = "stretch_done", nullable = false)
    private Integer stretchDone;

    @Column(name = "grade", length = 20)
    private String grade; // EXCELLENT, GOOD, NORMAL, BAD

    @Column(name = "llm_summary", columnDefinition = "TEXT")
    private String llmSummary;

    @Column(name = "llm_highlights", columnDefinition = "TEXT")
    private String llmHighlights; // JSON array string

    @Column(name = "llm_advice", columnDefinition = "TEXT")
    private String llmAdvice; // JSON array string

    @Column(name = "source", length = 20)
    private String source; // llm or rule_based

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public DailyReport(Member member, LocalDate reportDate, Double totalMonitoredMin, Double avgGoodRatio,
                       Integer totalAlerts, Integer stretchSuggested, Integer stretchDone, String grade,
                       String llmSummary, String llmHighlights, String llmAdvice, String source) {
        this.member = member;
        this.reportDate = reportDate;
        this.totalMonitoredMin = totalMonitoredMin != null ? totalMonitoredMin : 0.0;
        this.avgGoodRatio = avgGoodRatio != null ? avgGoodRatio : 0.0;
        this.totalAlerts = totalAlerts != null ? totalAlerts : 0;
        this.stretchSuggested = stretchSuggested != null ? stretchSuggested : 0;
        this.stretchDone = stretchDone != null ? stretchDone : 0;
        this.grade = grade != null ? grade : "NORMAL";
        this.llmSummary = llmSummary;
        this.llmHighlights = llmHighlights;
        this.llmAdvice = llmAdvice;
        this.source = source != null ? source : "rule_based";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateAnalysis(Double totalMonitoredMin, Double avgGoodRatio, Integer totalAlerts,
                               Integer stretchSuggested, Integer stretchDone, String grade,
                               String llmSummary, String llmHighlights, String llmAdvice, String source) {
        this.totalMonitoredMin = totalMonitoredMin;
        this.avgGoodRatio = avgGoodRatio;
        this.totalAlerts = totalAlerts;
        this.stretchSuggested = stretchSuggested;
        this.stretchDone = stretchDone;
        this.grade = grade;
        this.llmSummary = llmSummary;
        this.llmHighlights = llmHighlights;
        this.llmAdvice = llmAdvice;
        this.source = source;
        this.updatedAt = LocalDateTime.now();
    }
}
