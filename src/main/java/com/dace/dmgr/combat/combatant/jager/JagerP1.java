package com.dace.dmgr.combat.combatant.jager;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import lombok.NonNull;

import java.util.EnumSet;
import java.util.Set;

public final class JagerP1 extends AbstractSkill {
    /** 현재 사용 대상 */
    private Damageable target = null;

    public JagerP1(@NonNull CombatUser combatUser) {
        super(combatUser, JagerP1Info.getInstance(), Timespan.ZERO, Timespan.MAX);
    }

    @Override
    @NonNull
    public Set<@NonNull ActionKey> getDefaultActionKeys() {
        return EnumSet.of(ActionKey.PERIODIC_1);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && target.isCreature();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
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
        combatUser.getActionManager().useAction(ActionKey.PERIODIC_1);
    }
}
