package com.dace.dmgr.event;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.gui.menu.event.ChatSoundMenuEvent;
import com.dace.dmgr.gui.menu.event.MainMenuEvent;
import com.dace.dmgr.gui.menu.event.OptionMenuEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import static com.dace.dmgr.system.EntityList.combatUserList;

public class InventoryClick {
    public static void event(InventoryClickEvent event, Player player) {
        CombatUser combatUser = combatUserList.get(player.getUniqueId());

        if (combatUser != null)
            event.setCancelled(true);

        MainMenuEvent.getInstance().event(event, player);
        OptionMenuEvent.getInstance().event(event, player);
        ChatSoundMenuEvent.getInstance().event(event, player);
    }
}
