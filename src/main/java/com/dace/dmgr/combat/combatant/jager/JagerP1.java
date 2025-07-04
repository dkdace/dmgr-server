package com.dace.dmgr.combat.combatant.jager;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.PassiveSkill;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import lombok.NonNull;

public final class JagerP1 extends PassiveSkill {
    /** 현재 사용 대상 */
    private Damageable target = null;

    public JagerP1(@NonNull CombatUser combatUser, @NonNull JagerP1Info skillInfo) {
        super(combatUser, skillInfo, Timespan.ZERO, Timespan.MAX);
    }

    @Override
    protected boolean canUse() {
        return super.canUse() && target.isCreature();
    }

    @Override
    protected void onUse() {
        combatUser.setGlowing(target, JagerP1Info.DURATION);
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    /**
     * 스킬을 사용한다.
     *
     * @param target 사용 대상
     */
    void use(@NonNull Damageable target) {
        this.target = target;
        use(ActionKey.SYSTEM);
    }
}
