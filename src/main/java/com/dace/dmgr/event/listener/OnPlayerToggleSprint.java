package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.entity.CombatUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSprintEvent;

import static com.dace.dmgr.system.HashMapList.combatUserHashMap;

public class OnPlayerToggleSprint implements Listener {
    @EventHandler
    public static void event(PlayerToggleSprintEvent event) {
        CombatUser combatUser = combatUserHashMap.get(event.getPlayer());

        if (combatUser != null)
            combatUser.onToggleSprint(event.isSprinting());
    }
}
