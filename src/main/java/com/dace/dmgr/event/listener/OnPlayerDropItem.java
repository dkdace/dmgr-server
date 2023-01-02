package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.event.combatuser.CombatUserActionEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import static com.dace.dmgr.system.HashMapList.combatUserMap;

public class OnPlayerDropItem implements Listener {
    @EventHandler
    public static void event(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        CombatUser combatUser = combatUserMap.get(event.getPlayer());

        if (!player.isOp())
            event.setCancelled(true);

        if (combatUser != null) {
            event.setCancelled(true);
            if (combatUser.getCharacter() != null) {
                CombatUserActionEvent newEvent = new CombatUserActionEvent(combatUser, ActionKey.DROP);

                Bukkit.getServer().getPluginManager().callEvent(newEvent);
            }
        }
    }
}
