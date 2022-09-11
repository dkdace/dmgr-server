package com.dace.dmgr.event;

import com.dace.dmgr.combat.entity.CombatUser;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import static com.dace.dmgr.system.EntityList.combatUserList;

public class PlayerInteract {
    public static void event(PlayerInteractEvent event, Player player) {
        CombatUser combatUser = combatUserList.get(event.getPlayer().getUniqueId());

        if (combatUser != null) {
            if (event.hasBlock())
                event.setCancelled(true);
            switch (event.getAction()) {
                case LEFT_CLICK_AIR:
                case LEFT_CLICK_BLOCK:
                    combatUser.onLeftClick();
                    break;
                case RIGHT_CLICK_AIR:
                case RIGHT_CLICK_BLOCK:
                    combatUser.onRightClick();
                    break;
            }
        }
    }
}
