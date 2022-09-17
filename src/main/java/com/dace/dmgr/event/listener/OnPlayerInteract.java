package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.entity.CombatUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import static com.dace.dmgr.system.EntityList.combatUserList;

public class OnPlayerInteract implements Listener {
    @EventHandler
    public static void event(PlayerInteractEvent event) {
        CombatUser combatUser = combatUserList.get(event.getPlayer().getUniqueId());

        if (combatUser != null) {
            if (event.hasBlock())
                event.setCancelled(true);
            switch (event.getAction()) {
                case LEFT_CLICK_AIR:
                case LEFT_CLICK_BLOCK:
                    combatUser.onLeftClick();
                    break;
                case RIGHT_CLICK_AIR:
                case RIGHT_CLICK_BLOCK:
                    combatUser.onRightClick();
                    break;
            }
        }
    }
}
