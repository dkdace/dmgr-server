package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.user.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public final class OnPlayerToggleFlight implements Listener {
    @EventHandler
    public static void event(PlayerToggleFlightEvent event) {
        CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(event.getPlayer()));

        if (combatUser != null && combatUser.getCharacterType() != null) {
            event.setCancelled(true);
            combatUser.useAction(ActionKey.SPACE);
        }
    }
}
