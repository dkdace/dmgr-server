package com.dace.dmgr.combat.action;

import com.dace.dmgr.combat.entity.CombatUser;

public interface Reloadable {
    int getCapacity();

    long getReloadDuration();

    void reload(CombatUser combatUser, WeaponController weaponController);
}
