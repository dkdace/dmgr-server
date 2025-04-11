package com.dace.dmgr.combat.entity.temporary.dummy;

import com.dace.dmgr.combat.entity.Damageable;
import lombok.NonNull;

/**
 * 더미(훈련용 봇)의 행동 양식을 관리하는 인터페이스.
 */
public interface DummyBehavior {
    /**
     * 더미 생성 시 실행할 작업.
     *
     * @param dummy 대상 더미
     */
    default void onInit(@NonNull Dummy dummy) {
        // 미사용
    }

    /**
     * 더미가 기본 공격을 했을 때 실행할 작업.
     *
     * @param dummy  대상 더미
     * @param victim 피격자
     */
    default void onDefaultAttack(@NonNull Dummy dummy, @NonNull Damageable victim) {
        // 미사용
    }
}
