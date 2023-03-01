package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.entity.CombatUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import static com.dace.dmgr.system.HashMapList.combatUserMap;

public class OnInventoryClick implements Listener {
    @EventHandler
    public static void event(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        CombatUser combatUser = combatUserMap.get(player);

        if (combatUser != null)
            event.setCancelled(true);
    }
}
