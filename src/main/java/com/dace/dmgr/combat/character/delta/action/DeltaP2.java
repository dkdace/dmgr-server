package com.dace.dmgr.combat.character.delta.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.NonNull;

public class DeltaP2 extends AbstractSkill {
    public DeltaP2(CombatUser combatUser) {
        super(combatUser, DeltaP2Info.getInstance());
    }

    @Override
    public @NonNull ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[] {ActionKey.PERIODIC_2};
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

    }

}

