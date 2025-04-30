package com.dace.dmgr.combat.combatant.delta;

import com.dace.dmgr.combat.action.Trait;
import com.dace.dmgr.combat.action.info.DynamicTraitInfo;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import lombok.NonNull;

public final class DeltaT1 extends Trait {
    public DeltaT1(@NonNull CombatUser combatUser, @NonNull DynamicTraitInfo<?> traitInfo) {
        super(combatUser, traitInfo);
    }
}
