package com.dace.dmgr.combat.entity;

import com.dace.dmgr.combat.entity.module.HealModule;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * 다른 엔티티로부터 피해 및 치유를 받을 수 있는 엔티티의 인터페이스.
 */
public interface Healable extends Damageable {
    /**
     * @return 회복 모듈
     */
    @NonNull
    HealModule getDamageModule();

    /**
     * 엔티티가 치유를 받았을 때 실행될 작업.
     *
     * @param provider 제공자
     * @param amount   치유량
     * @see Healer#onGiveHeal(Healable, double, boolean)
     */
    void onTakeHeal(@Nullable Healer provider, double amount);
}
