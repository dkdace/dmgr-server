package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.entity.CombatUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSprintEvent;

import static com.dace.dmgr.system.EntityList.combatUserList;

public class OnPlayerToggleSprint implements Listener {
    @EventHandler
    public static void event(PlayerToggleSprintEvent event) {
        Player player = event.getPlayer();
        CombatUser combatUser = combatUserList.get(player.getUniqueId());

        if (combatUser != null)
            combatUser.onToggleSprint(event.isSprinting());
    }
}
