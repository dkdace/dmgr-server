package com.dace.dmgr.combat.character.palas.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Healable;
import lombok.NonNull;
import lombok.Setter;

@Setter
public final class PalasP1 extends AbstractSkill {
    /** 현재 사용 대상 */
    private Healable target = null;
    /** 최근 치유량 */
    private int healAmount;

    public PalasP1(@NonNull CombatUser combatUser) {
        super(combatUser);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.PERIODIC_1};
    }

    @Override
    public long getDefaultCooldown() {
        return 0;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        target.getDamageModule().heal(combatUser, healAmount, true);
    }

    @Override
    public boolean isCancellable() {
        return false;
    }
}
