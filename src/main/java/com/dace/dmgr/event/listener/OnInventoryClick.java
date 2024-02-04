package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public final class OnInventoryClick implements Listener {
    @EventHandler
    public static void event(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(player));

        if (combatUser != null)
            event.setCancelled(true);
    }
}
