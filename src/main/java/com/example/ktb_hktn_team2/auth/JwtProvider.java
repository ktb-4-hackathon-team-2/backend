package com.example.ktb_hktn_team2.auth;

import com.example.ktb_hktn_team2.common.ApiException;
import com.example.ktb_hktn_team2.common.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

/**
 * Access Token 발급 / 검증. HS256 서명을 사용한다.
 */
@Component
public class JwtProvider {

    private static final int MIN_SECRET_BYTES = 32;

    private final SecretKey secretKey;
    private final Duration accessTokenExpiration;

    public JwtProvider(JwtProperties jwtProperties) {
        byte[] secretBytes = jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "app.jwt.secret 은 최소 " + MIN_SECRET_BYTES + "바이트 이상이어야 합니다. (현재 " + secretBytes.length + "바이트)");
        }

        this.secretKey = Keys.hmacShaKeyFor(secretBytes);
        this.accessTokenExpiration = jwtProperties.accessTokenExpiration();
    }

    /**
     * subject 에 회원 PK 를 담는다. 이후 요청에서는 이 값으로 회원 DB 를 조회한다.
     */
    public String createAccessToken(Long memberId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpiration.toMillis());

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 서명/만료를 검증하고 회원 PK 를 꺼낸다.
     *
     * @throws ApiException 토큰이 만료되었거나 유효하지 않은 경우
     */
    public Long parseMemberId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return Long.valueOf(claims.getSubject());
        } catch (ExpiredJwtException e) {
            throw new ApiException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ApiException(ErrorCode.INVALID_TOKEN);
        }
    }

    public long getAccessTokenExpiresInSeconds() {
        return accessTokenExpiration.toSeconds();
    }
}
