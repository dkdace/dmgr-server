package com.dace.dmgr.combat.event;

import com.dace.dmgr.combat.entity.CombatUser;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 전투 관련 플레이어 이벤트.
 */
public class CombatUserEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    /** 이벤트를 호출한 플레이어 */
    protected final CombatUser combatUser;

    /**
     * 이벤트를 생성한다.
     *
     * @param combatUser 이벤트를 호출한 플레이어
     */
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
