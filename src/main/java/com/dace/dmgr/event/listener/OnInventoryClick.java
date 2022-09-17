package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.gui.menu.event.ChatSoundMenuEvent;
import com.dace.dmgr.gui.menu.event.MainMenuEvent;
import com.dace.dmgr.gui.menu.event.OptionMenuEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import static com.dace.dmgr.system.HashMapList.combatUserHashMap;

public class OnInventoryClick implements Listener {
    @EventHandler
    public static void event(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        CombatUser combatUser = combatUserHashMap.get(player);

        if (combatUser != null)
            event.setCancelled(true);

        MainMenuEvent.getInstance().event(event, player);
        OptionMenuEvent.getInstance().event(event, player);
        ChatSoundMenuEvent.getInstance().event(event, player);
    }
}
