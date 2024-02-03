package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.user.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSprintEvent;

public final class OnPlayerToggleSprint implements Listener {
    @EventHandler
    public static void event(PlayerToggleSprintEvent event) {
        CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(event.getPlayer()));

        if (combatUser != null && combatUser.getCharacterType() != null)
            combatUser.useAction(ActionKey.SPRINT);
    }
}
