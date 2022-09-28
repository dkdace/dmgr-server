package com.dace.dmgr.combat.event.combatuser;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.event.CombatUserEvent;
import org.bukkit.event.HandlerList;

public class CombatUserActionEvent extends CombatUserEvent {
    private static final HandlerList handlers = new HandlerList();
    private final ActionKey actionKey;

    public CombatUserActionEvent(CombatUser combatUser, ActionKey actionKey) {
        super(combatUser);
        this.actionKey = actionKey;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public ActionKey getActionKey() {
        return actionKey;
    }
}
