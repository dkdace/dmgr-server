package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.entity.CombatUser;

public interface HasSprintEvent {
    void onSprintToggle(CombatUser combatUser, boolean sprint);
}
