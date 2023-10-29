package com.dace.dmgr.combat.entity.module;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatEntityBase;

/**
 * 엔티티 모듈의 인터페이스.
 */
public interface CombatEntityModule {
    /**
     * {@link CombatEntity#init()} 호출 시 실행할 작업.
     */
    default void onInit() {
    }

    /**
     * {@link CombatEntityBase#onTick(int)} 호출 시 실행할 작업.
     *
     * @param i 인덱스
     */
    default void onTick(int i) {
    }

    /**
     * {@link CombatEntity#remove()} 호출 시 실행할 작업.
     */
    default void onRemove() {
    }
}
