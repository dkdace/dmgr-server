package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.event.combatuser.CombatUserActionEvent;
import com.dace.dmgr.system.EntityInfoRegistry;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class OnPlayerInteract implements Listener {
    @EventHandler
    public static void event(PlayerInteractEvent event) {
        CombatUser combatUser = EntityInfoRegistry.getCombatUser(event.getPlayer());

        if (combatUser != null) {
            if (event.hasBlock())
                event.setCancelled(true);

            if (combatUser.getCharacter() != null) {
                ActionKey actionKey = null;

                switch (event.getAction()) {
                    case LEFT_CLICK_AIR:
                    case LEFT_CLICK_BLOCK:
                        actionKey = ActionKey.LEFT_CLICK;
                        break;
                    case RIGHT_CLICK_AIR:
                    case RIGHT_CLICK_BLOCK:
                        actionKey = ActionKey.RIGHT_CLICK;
                        break;

                }

                CombatUserActionEvent newEvent = new CombatUserActionEvent(combatUser, actionKey);

                Bukkit.getServer().getPluginManager().callEvent(newEvent);
            }
        }
    }
}
