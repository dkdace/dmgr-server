package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.event.combatuser.CombatUserActionEvent;
import com.dace.dmgr.system.EntityInfoRegistry;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

public final class OnPlayerItemHeld implements Listener {
    @EventHandler
    public static void event(PlayerItemHeldEvent event) {
        CombatUser combatUser = EntityInfoRegistry.getCombatUser(event.getPlayer());
        int newSlot = event.getNewSlot();

        if (combatUser != null) {
            event.setCancelled(true);

            if (combatUser.getCharacter() != null && newSlot < 4) {
                ActionKey actionKey = null;

                switch (newSlot) {
                    case 0:
                        actionKey = ActionKey.SLOT_1;
                        break;
                    case 1:
                        actionKey = ActionKey.SLOT_2;
                        break;
                    case 2:
                        actionKey = ActionKey.SLOT_3;
                        break;
                    case 3:
                        actionKey = ActionKey.SLOT_4;
                        break;
                }

                CombatUserActionEvent newEvent = new CombatUserActionEvent(combatUser, actionKey);

                Bukkit.getServer().getPluginManager().callEvent(newEvent);
            }
        }
    }
}
