package com.dace.dmgr.combat.combatant.vellion;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import lombok.NonNull;

import java.util.EnumSet;
import java.util.Set;

public final class VellionP2 extends AbstractSkill {
    /** 최근 피해량 */
    private double damageAmount;

    public VellionP2(@NonNull CombatUser combatUser) {
        super(combatUser, VellionP2Info.getInstance(), Timespan.ZERO, Timespan.MAX);
    }

    @Override
    @NonNull
    public Set<@NonNull ActionKey> getDefaultActionKeys() {
        return EnumSet.of(ActionKey.PERIODIC_1);
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        combatUser.getHealModule().heal(combatUser, damageAmount * VellionP2Info.HEAL_DAMAGE_RATIO, false);
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    /**
     * 다른 엔티티를 공격했을 때 실행할 작업.
     *
     * @param victim 피격자
     * @param damage 피해량
     */
    void onAttack(@NonNull Damageable victim, double damage) {
        if (combatUser == victim || !victim.isCreature())
            return;

        damageAmount = damage;
        combatUser.getActionManager().useAction(ActionKey.PERIODIC_1);
    }
}
