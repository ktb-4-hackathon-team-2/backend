package com.example.ktb_hktn_team2.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueStatDto {
    private String code; // "neck_tilt", "shoulder_tilt", "head_down", "lean_in"
    private String label; // "거북목 · 목 기울임", "어깨 기울어짐"
    private Integer count; // 발생 횟수
    private Integer ratio; // 비율 %
    private String description; // 원인 설명
    private String recommendedStretch; // 권장 스트레칭
    private String stretchId; // 스트레칭 ID (chin_tuck, shoulder_shrug 등)
}
