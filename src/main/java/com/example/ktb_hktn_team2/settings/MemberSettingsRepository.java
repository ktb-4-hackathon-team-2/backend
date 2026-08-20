package com.example.ktb_hktn_team2.settings;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * PK 가 member.id 라 findById(memberId) 로 바로 조회한다.
 */
public interface MemberSettingsRepository extends JpaRepository<MemberSettings, Long> {
}
