package com.example.ktb_hktn_team2.settings;

import com.example.ktb_hktn_team2.member.Member;
import com.example.ktb_hktn_team2.settings.dto.SettingsRequest;
import com.example.ktb_hktn_team2.settings.dto.SettingsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final MemberSettingsRepository memberSettingsRepository;
    private final TransactionTemplate transactionTemplate;

    /**
     * 저장한 적이 없으면 기본값을 내려준다. (행을 만들지는 않는다 — 실제 저장은 PUT 에서만)
     */
    @Transactional(readOnly = true)
    public SettingsResponse get(Member member) {
        return memberSettingsRepository.findById(member.getId())
                .map(SettingsResponse::from)
                .orElseGet(SettingsResponse::defaults);
    }

    /**
     * 업서트. 회원당 1행이라 있으면 갱신, 없으면 기본값 행을 만든 뒤 요청값으로 덮어쓴다.
     * <p>
     * 저장 버튼 연타 등으로 같은 회원의 요청이 동시에 들어오면 양쪽 다 "행 없음"을 보고 INSERT 를 시도해
     * 한쪽이 PK 제약에 걸린다. 이때는 이미 행이 생긴 상태이므로 새 트랜잭션에서 한 번 더 시도하면 UPDATE 로 처리된다.
     * (제약 위반이 난 트랜잭션은 rollback-only 라 같은 트랜잭션 안에서는 재시도할 수 없다.)
     */
    public SettingsResponse save(Member member, SettingsRequest request) {
        try {
            return transactionTemplate.execute(status -> upsert(member, request));
        } catch (DataIntegrityViolationException e) {
            return transactionTemplate.execute(status -> upsert(member, request));
        }
    }

    private SettingsResponse upsert(Member member, SettingsRequest request) {
        MemberSettings settings = memberSettingsRepository.findById(member.getId())
                .orElseGet(() -> memberSettingsRepository.save(MemberSettings.defaultsOf(member)));

        settings.update(request.sensitivity(), AlertSound.from(request.sound()),
                request.maxAlertLevel(), request.stretchMin());

        return SettingsResponse.from(settings);
    }
}
