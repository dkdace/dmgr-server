package com.dace.dmgr.event;

import com.dace.dmgr.combat.entity.CombatUser;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemHeldEvent;

import static com.dace.dmgr.system.EntityList.combatUserList;

public class PlayerItemHeld {
    public static void event(PlayerItemHeldEvent event, Player player) {
        CombatUser combatUser = combatUserList.get(event.getPlayer().getUniqueId());

        if (combatUser != null) {
            event.setCancelled(true);
            combatUser.onItemHeld(event.getNewSlot());
        }
    }
}
