package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.event.combatuser.CombatUserActionEvent;
import com.dace.dmgr.system.EntityInfoRegistry;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public final class OnPlayerInteract implements Listener {
    @EventHandler
    public static void event(PlayerInteractEvent event) {
        CombatUser combatUser = EntityInfoRegistry.getCombatUser(event.getPlayer());

        if (combatUser != null) {
            if (event.hasBlock())
                event.setCancelled(true);

            if (combatUser.getCharacter() != null && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                CombatUserActionEvent newEvent = new CombatUserActionEvent(combatUser, ActionKey.RIGHT_CLICK);

                Bukkit.getServer().getPluginManager().callEvent(newEvent);
            }
        }
    }
}
