package com.example.ktb_hktn_team2.report.repository;

import com.example.ktb_hktn_team2.member.Member;
import com.example.ktb_hktn_team2.report.domain.PostureMinuteLog;
import com.example.ktb_hktn_team2.report.dto.HourlyStatDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostureMinuteLogRepository extends JpaRepository<PostureMinuteLog, Long> {

    // 1. 특정 기간 동안의 0~23시 시간대별 통계
    @Query("SELECT new com.example.ktb_hktn_team2.report.dto.HourlyStatDto(" +
            "HOUR(p.loggedAt), " +
            "CASE WHEN SUM(p.monitoredSec) > 0 THEN CAST(SUM(p.goodSec) AS double) / SUM(p.monitoredSec) ELSE 0.0 END, " +
            "CAST(SUM(p.monitoredSec) AS double) / 60.0, " +
            "SUM(p.alertCount)) " +
            "FROM PostureMinuteLog p " +
            "WHERE p.member = :member AND p.loggedAt >= :start AND p.loggedAt <= :end " +
            "GROUP BY HOUR(p.loggedAt) " +
            "ORDER BY HOUR(p.loggedAt) ASC")
    List<HourlyStatDto> findHourlyStatsBetween(
            @Param("member") Member member,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // 2. 특정 기간 동안의 총합 수치 조회
    @Query("SELECT " +
            "COALESCE(SUM(p.monitoredSec), 0), " +
            "COALESCE(SUM(p.goodSec), 0), " +
            "COALESCE(SUM(p.alertCount), 0) " +
            "FROM PostureMinuteLog p " +
            "WHERE p.member = :member AND p.loggedAt >= :start AND p.loggedAt <= :end")
    List<Object[]> findSummaryBetween(
            @Param("member") Member member,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // 3. 특정 기간 동안 기록된 모든 issue_codes 문자열 조회
    @Query("SELECT p.issueCodes FROM PostureMinuteLog p " +
            "WHERE p.member = :member AND p.loggedAt >= :start AND p.loggedAt <= :end AND p.issueCodes IS NOT NULL AND p.issueCodes != ''")
    List<String> findIssueCodesBetween(
            @Param("member") Member member,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
