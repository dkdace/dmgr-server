package com.dace.dmgr.combat.event;

import com.dace.dmgr.combat.entity.CombatUser;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CombatUserEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    protected final CombatUser combatUser;

    public CombatUserEvent(CombatUser combatUser) {
        this.combatUser = combatUser;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public final CombatUser getCombatUser() {
        return combatUser;
    }
}
