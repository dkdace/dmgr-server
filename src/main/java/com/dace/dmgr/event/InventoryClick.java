package com.dace.dmgr.event;

import com.dace.dmgr.combat.CombatUser;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import static com.dace.dmgr.system.EntityList.combatUserList;

public class InventoryClick {
    public static void event(InventoryClickEvent event, Player player) {
        CombatUser combatUser = combatUserList.get(player.getUniqueId());

        if (combatUser != null)
            event.setCancelled(true);
    }
}
