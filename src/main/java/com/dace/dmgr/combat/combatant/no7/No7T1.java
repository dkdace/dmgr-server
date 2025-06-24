package com.dace.dmgr.combat.combatant.no7;

import com.dace.dmgr.combat.ability.Trait;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.DamageModule;
import lombok.NonNull;

public final class No7T1 extends Trait {
    /** 보호막 */
    private final DamageModule.Shield shield;

    public No7T1(@NonNull CombatUser combatUser, @NonNull No7T1Info traitInfo) {
        super(combatUser, traitInfo);
        this.shield = combatUser.getDamageModule().createShield(0);
    }

    /**
     * 남은 보호막 체력을 반환한다.
     *
     * @return 남은 보호막 체력
     */
    double getShield() {
        return shield.getHealth();
    }

    /**
     * 지정한 양만큼 보호막을 증가시킨다.
     *
     * @param amount 보호막
     */
    void addShield(double amount) {
        shield.setHealth(Math.min(No7T1Info.MAX_SHIELD, shield.getHealth() + amount));
    }

    /**
     * 매 틱마다 실행할 작업.
     */
    void onTick() {
        if (!combatUser.getDamageModule().isLowHealth() || getShield() > No7P1Info.SHIELD)
            addShield(-No7T1Info.DECREASE_PER_SECOND / 20.0);
    }
}
