package com.dace.dmgr.combat.entity.module;

import com.dace.dmgr.combat.entity.Attacker;
import lombok.NonNull;

/**
 * 공격할 수 있는 엔티티의 모듈 클래스.
 *
 * @see Attacker
 */
public final class AttackerModule extends CombatEntityModule<Attacker> {
    /**
     * 공격 모듈 인스턴스를 생성한다.
     */
    public AttackerModule(@NonNull Attacker combatEntity) {
        super(combatEntity);
    }

    @Override
    protected double getBaseValue() {
        return 1;
    }
}
