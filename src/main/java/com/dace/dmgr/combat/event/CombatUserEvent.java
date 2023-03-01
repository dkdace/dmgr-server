package com.dace.dmgr.combat.event;

import com.dace.dmgr.combat.entity.CombatUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 전투 관련 플레이어 이벤트.
 */
@AllArgsConstructor
@Getter
public class CombatUserEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    /** 이벤트를 호출한 플레이어 */
    protected final CombatUser combatUser;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
