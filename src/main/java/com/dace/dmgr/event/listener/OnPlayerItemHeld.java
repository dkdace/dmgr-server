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
        CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(event.getPlayer()));
        int newSlot = event.getNewSlot();

        if (combatUser == null)
            return;

        event.setCancelled(true);
        if (combatUser.getCharacterType() == null || newSlot >= 4)
            return;

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
            default:
                break;
        }
    }
}
