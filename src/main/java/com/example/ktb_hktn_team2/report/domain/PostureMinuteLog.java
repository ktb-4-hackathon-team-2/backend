package com.example.ktb_hktn_team2.report.domain;

import com.example.ktb_hktn_team2.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "posture_minute_log",
        indexes = {
                @Index(name = "idx_member_logged_at", columnList = "member_id, logged_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostureMinuteLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "logged_at", nullable = false)
    private LocalDateTime loggedAt;

    @Column(name = "monitored_sec", nullable = false)
    private Integer monitoredSec; // 1분간 실제 모니터링된 초 (예: 60)

    @Column(name = "good_sec", nullable = false)
    private Integer goodSec; // 바른 자세 유지 초 (예: 48)

    @Column(name = "alert_count", nullable = false)
    private Integer alertCount; // 발생한 경고 횟수

    @Column(name = "issue_codes", length = 200)
    private String issueCodes; // 감지된 나쁜 자세 코드 목록 (콤마 구분: neck_tilt,shoulder_tilt)

    @Builder
    public PostureMinuteLog(Member member, LocalDateTime loggedAt, Integer monitoredSec, Integer goodSec, Integer alertCount, String issueCodes) {
        this.member = member;
        this.loggedAt = loggedAt != null ? loggedAt : LocalDateTime.now();
        this.monitoredSec = monitoredSec != null ? monitoredSec : 0;
        this.goodSec = goodSec != null ? goodSec : 0;
        this.alertCount = alertCount != null ? alertCount : 0;
        this.issueCodes = issueCodes;
    }
}
