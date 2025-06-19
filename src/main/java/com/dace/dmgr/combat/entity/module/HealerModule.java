package com.dace.dmgr.combat.entity.module;

import com.dace.dmgr.combat.entity.Healer;
import lombok.NonNull;

/**
 * 치유할 수 있는 엔티티의 모듈 클래스.
 *
 * @see Healer
 */
public final class HealerModule extends CombatEntityModule<Healer> {
    /**
     * 치유 모듈 인스턴스를 생성한다.
     */
    public HealerModule(@NonNull Healer combatEntity) {
        super(combatEntity);
    }

    @Override
    protected double getBaseValue() {
        return 1;
    }
}
