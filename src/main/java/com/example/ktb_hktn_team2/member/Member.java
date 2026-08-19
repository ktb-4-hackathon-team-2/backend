package com.example.ktb_hktn_team2.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "member",
        uniqueConstraints = @UniqueConstraint(name = "uk_member_email", columnNames = "email")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    /**
     * 평문이 아니라 PBKDF2 로 해싱된 값이 저장된다. ({@code iterations$salt$hash} 형식)
     */
    @Column(name = "pw", nullable = false, length = 200)
    private String pw;

    private Member(String email, String pw) {
        this.email = email;
        this.pw = pw;
    }

    public static Member of(String email, String encodedPw) {
        return new Member(email, encodedPw);
    }
}
