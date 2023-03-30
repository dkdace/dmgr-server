package com.dace.dmgr.combat.event;

import com.dace.dmgr.combat.event.listener.OnCombatUserAction;
import com.dace.dmgr.event.EventUtil;
import com.kiwi.dmgr.game.event.listener.*;

/**
 * 전투 관련 이벤트를 등록하는 클래스.
 */
public class CombatEventManager {
    public static void init() {
        EventUtil.registerListener(new OnCombatUserAction());

        EventUtil.registerListener(new OnGameUserAttack());
        EventUtil.registerListener(new OnGameUserKill());
        EventUtil.registerListener(new OnGameUserDeath());
        EventUtil.registerListener(new OnGameUserAssist());
        EventUtil.registerListener(new OnGameUserHeal());
    }
}
