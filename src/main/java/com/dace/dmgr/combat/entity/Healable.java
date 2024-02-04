package com.dace.dmgr.combat.entity;

import com.dace.dmgr.combat.entity.module.HealModule;
import lombok.NonNull;

/**
 * 다른 엔티티로부터 치유를 받을 수 있는 엔티티의 인터페이스.
 */
public interface Healable extends Damageable {
    /**
     * @return 치유 모듈
     */
    @NonNull
    HealModule getDamageModule();

    /**
     * 엔티티가 치유를 받았을 때 실행될 작업.
     *
     * @param provider 제공자
     * @param amount   치유량
     * @param isUlt    궁극기 충전 여부
     * @see Healer#onGiveHeal(Healable, int, boolean)
     */
    void onTakeHeal(Healer provider, int amount, boolean isUlt);
}
