package com.dace.dmgr.combat.combatant.no7;

import com.dace.dmgr.combat.action.Trait;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.DamageModule;
import lombok.NonNull;

public final class No7T1 extends Trait {
    /** 보호막 */
    private final DamageModule.Shield shield;

    public No7T1(@NonNull CombatUser combatUser) {
        super(combatUser, No7T1Info.getInstance());
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
}
