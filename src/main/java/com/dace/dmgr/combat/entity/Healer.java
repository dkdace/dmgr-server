package com.dace.dmgr.combat.entity;

import com.dace.dmgr.combat.entity.module.HealerModule;
import lombok.NonNull;

/**
 * 다른 엔티티를 치유할 수 있는 엔티티의 인터페이스.
 */
public interface Healer extends CombatEntity {
    /**
     * @return 치유 모듈
     */
    @NonNull
    HealerModule getHealerModule();

    /**
     * 엔티티가 다른 엔티티를 치유했을 때 실행될 작업.
     *
     * @param target 수급자
     * @param amount 치유량
     * @param isUlt  궁극기 충전 여부
     * @see Healable#onTakeHeal(Healer, double)
     */
    void onGiveHeal(@NonNull Healable target, double amount, boolean isUlt);
}
