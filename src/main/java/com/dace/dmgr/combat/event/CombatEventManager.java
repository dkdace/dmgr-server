package com.dace.dmgr.combat.event;

import com.dace.dmgr.combat.event.listener.OnCombatUserAction;
import com.dace.dmgr.event.EventUtil;

/**
 * 전투 관련 이벤트를 등록하는 클래스.
 */
public final class CombatEventManager {
    public static void init() {
        EventUtil.registerListener(new OnCombatUserAction());
    }
}
