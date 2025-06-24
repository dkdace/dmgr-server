package com.dace.dmgr.combat.combatant.palas;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.PassiveSkill;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import lombok.NonNull;

import java.util.EnumSet;
import java.util.Set;

public final class PalasP1 extends PassiveSkill {
    /** 현재 사용 대상 */
    private Healable target = null;
    /** 최근 치유량 */
    private double healAmount;

    public PalasP1(@NonNull CombatUser combatUser, @NonNull PalasP1Info skillInfo) {
        super(combatUser, skillInfo, Timespan.ZERO, Timespan.MAX);
    }

    @Override
    @NonNull
    public Set<@NonNull ActionKey> getDefaultActionKeys() {
        return EnumSet.of(ActionKey.PERIODIC_1);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && target.getDamageModule().isLowHealth();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        target.getHealModule().heal(combatUser, healAmount, true);
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    /**
     * 스킬을 사용한다.
     *
     * @param target     사용 대상
     * @param healAmount 치유량
     */
    void use(@NonNull Healable target, double healAmount) {
        this.target = target;
        this.healAmount = healAmount;

        combatUser.getAbilityManager().useAction(ActionKey.PERIODIC_1);
    }
}
