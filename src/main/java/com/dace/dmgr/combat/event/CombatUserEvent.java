package com.dace.dmgr.combat.event;

import com.dace.dmgr.combat.entity.CombatUser;
import org.bukkit.event.Event;

public abstract class CombatUserEvent extends Event {
    protected CombatUser combatUser;

    public CombatUserEvent(CombatUser combatUser) {
        this.combatUser = combatUser;
    }

    public final CombatUser getCombatUser() {
        return combatUser;
    }
}
