package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.user.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

public final class OnPlayerItemHeld implements Listener {
    @EventHandler
    public static void event(PlayerItemHeldEvent event) {
        User user = User.fromPlayer(event.getPlayer());
        CombatUser combatUser = CombatUser.fromUser(user);
        int newSlot = event.getNewSlot();

        if (combatUser != null) {
            event.setCancelled(true);

            if (combatUser.getCharacterType() != null && newSlot < 4) {
                switch (newSlot) {
                    case 0:
                        combatUser.useAction(ActionKey.SLOT_1);
                        break;
                    case 1:
                        combatUser.useAction(ActionKey.SLOT_2);
                        break;
                    case 2:
                        combatUser.useAction(ActionKey.SLOT_3);
                        break;
                    case 3:
                        combatUser.useAction(ActionKey.SLOT_4);
                        break;
                }
            }
        }
    }
}
