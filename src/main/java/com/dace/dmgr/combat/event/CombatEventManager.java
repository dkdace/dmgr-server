package com.dace.dmgr.combat.event;

import com.dace.dmgr.combat.event.listener.OnCombatUserAction;
import com.dace.dmgr.event.EventManager;

public class CombatEventManager extends EventManager {
    public static void init() {
        registerListener(new OnCombatUserAction());
    }
}
