package com.dace.dmgr.combat.combatant.delta;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import lombok.NonNull;

public final class DeltaWeapon extends AbstractWeapon {
    public DeltaWeapon(@NonNull CombatUser combatUser) {
        super(combatUser, DeltaWeaponInfo.getInstance(), DeltaWeaponInfo.COOLDOWN);
    }

    @Override
    public @NonNull ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[0];
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {

    }
}
