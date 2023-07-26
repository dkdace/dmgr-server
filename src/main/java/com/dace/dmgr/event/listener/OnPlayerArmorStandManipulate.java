package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.event.combatuser.CombatUserActionEvent;
import com.dace.dmgr.system.EntityInfoRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

public class OnPlayerArmorStandManipulate implements Listener {
    @EventHandler
    public static void event(PlayerArmorStandManipulateEvent event) {
        Player player = event.getPlayer();
        CombatUser combatUser = EntityInfoRegistry.getCombatUser(event.getPlayer());

        if (!player.isOp())
            event.setCancelled(true);

        if (combatUser != null) {
            event.setCancelled(true);
            if (combatUser.getCharacter() != null) {
                CombatUserActionEvent newEvent = new CombatUserActionEvent(combatUser, ActionKey.RIGHT_CLICK);

                Bukkit.getServer().getPluginManager().callEvent(newEvent);
            }
        }
    }
}
