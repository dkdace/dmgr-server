package com.dace.dmgr.combat.character.delta.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.NonNull;

import java.util.Arrays;

public final class DeltaP1 extends AbstractSkill {

    public DeltaP1(@NonNull CombatUser combatUser) {
        super(combatUser, DeltaP1Info.getInstance());
    }

    @Override
    public @NonNull ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[] {ActionKey.PERIODIC_1};
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
        setDuration();
    }
}
