package com.example.ktb_hktn_team2.settings;

import com.example.ktb_hktn_team2.member.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * 회원당 1행. member.id 를 그대로 PK 겸 FK 로 쓴다.
 * <p>
 * 네 항목 모두 기본값을 가지고 있어서, 저장한 적이 없어도 {@code GET /api/settings} 는 기본값을 내려준다.
 * 기본값을 바꾸려면 아래 상수만 수정하면 된다. (프론트 기본값과 반드시 일치시킬 것)
 */
@Entity
@Table(name = "member_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSettings {

    public static final int DEFAULT_SENSITIVITY = 50;
    public static final AlertSound DEFAULT_SOUND = AlertSound.CHIME;
    public static final int DEFAULT_MAX_ALERT_LEVEL = 2; // 3단계는 옵트인이라 기본에서 제외
    public static final int DEFAULT_STRETCH_MIN = 50;

    @Id
    @Column(name = "member_id")
    private Long memberId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "sensitivity", nullable = false)
    @ColumnDefault("50")
    private Integer sensitivity = DEFAULT_SENSITIVITY; // 0 ~ 100

    // JdbcTypeCode 를 안 주면 Hibernate 가 MySQL 네이티브 enum 컬럼으로 만든다.
    // 그러면 나중에 알림음을 추가할 때 ddl-auto:update 가 컬럼 타입을 못 바꿔서 insert 가 깨진다.
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "sound", nullable = false, length = 20)
    @ColumnDefault("'CHIME'")
    private AlertSound sound = DEFAULT_SOUND;

    @Column(name = "max_alert_level", nullable = false)
    @ColumnDefault("2")
    private Integer maxAlertLevel = DEFAULT_MAX_ALERT_LEVEL; // 1 ~ 3

    @Column(name = "stretch_min", nullable = false)
    @ColumnDefault("50")
    private Integer stretchMin = DEFAULT_STRETCH_MIN; // 30 ~ 90 (10 단위)

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private MemberSettings(Member member) {
        this.member = member;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 네 항목이 모두 기본값으로 채워진 설정. 저장하지 않고 응답용으로만 쓸 수도 있다.
     */
    public static MemberSettings defaultsOf(Member member) {
        return new MemberSettings(member);
    }

    public void update(Integer sensitivity, AlertSound sound, Integer maxAlertLevel, Integer stretchMin) {
        this.sensitivity = sensitivity;
        this.sound = sound;
        this.maxAlertLevel = maxAlertLevel;
        this.stretchMin = stretchMin;
        this.updatedAt = LocalDateTime.now();
    }
}
