package com.example.ktb_hktn_team2.report.repository;

import com.example.ktb_hktn_team2.member.Member;
import com.example.ktb_hktn_team2.report.domain.DailyReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyReportRepository extends JpaRepository<DailyReport, Long> {

    Optional<DailyReport> findByMemberAndReportDate(Member member, LocalDate reportDate);

    List<DailyReport> findByMemberAndReportDateBetweenOrderByReportDateAsc(
            Member member, LocalDate startDate, LocalDate endDate
    );
}
