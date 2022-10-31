package com.dace.dmgr.combat.event.combatuser;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.event.CombatUserEvent;

public class CombatUserActionEvent extends CombatUserEvent {
    private final ActionKey actionKey;

    public CombatUserActionEvent(CombatUser combatUser, ActionKey actionKey) {
        super(combatUser);
        this.actionKey = actionKey;
    }

    public ActionKey getActionKey() {
        return actionKey;
    }
}
