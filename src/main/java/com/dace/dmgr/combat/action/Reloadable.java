package com.dace.dmgr.combat.action;

import com.dace.dmgr.combat.entity.CombatUser;

public interface Reloadable {
    int getCapacity();

    long getReloadDuration();

    long getReloadDurationFull();

    void onReload(CombatUser combatUser, WeaponController weaponController);
}
