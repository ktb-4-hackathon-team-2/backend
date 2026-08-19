package com.example.ktb_hktn_team2.member;

import com.example.ktb_hktn_team2.common.ApiException;
import com.example.ktb_hktn_team2.common.ErrorCode;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * 비밀번호 해싱. Spring Security 를 쓰지 않으므로 JDK 표준 PBKDF2 로 직접 구현한다.
 * <p>
 * 저장 형식: {@code iterations$base64(salt)$base64(hash)}
 * salt 를 함께 저장하므로 같은 비밀번호라도 매번 다른 문자열이 저장된다.
 */
@Component
public class PasswordEncoder {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 100_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;
    private static final String DELIMITER = "\\$";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getEncoder();
    private static final Base64.Decoder DECODER = Base64.getDecoder();

    public String encode(String rawPassword) {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(salt);

        byte[] hash = hash(rawPassword, salt, ITERATIONS);
        return ITERATIONS + "$" + ENCODER.encodeToString(salt) + "$" + ENCODER.encodeToString(hash);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        String[] parts = encodedPassword.split(DELIMITER);
        if (parts.length != 3) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, "저장된 비밀번호 형식이 올바르지 않습니다.");
        }

        int iterations = Integer.parseInt(parts[0]);
        byte[] salt = DECODER.decode(parts[1]);
        byte[] expectedHash = DECODER.decode(parts[2]);

        // 길이에 따라 실행 시간이 달라지지 않도록 상수 시간 비교를 사용한다.
        return MessageDigest.isEqual(expectedHash, hash(rawPassword, salt, iterations));
    }

    private byte[] hash(String rawPassword, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(rawPassword.toCharArray(), salt, iterations, KEY_LENGTH_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("비밀번호 해싱에 실패했습니다.", e);
        } finally {
            spec.clearPassword();
        }
    }
}
