package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public final class OnPlayerDropItem implements Listener {
    @EventHandler
    public static void event(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(player));

        if (!player.isOp())
            event.setCancelled(true);

        if (combatUser != null) {
            event.setCancelled(true);
            if (combatUser.getCharacterType() != null)
                combatUser.useAction(ActionKey.DROP);
        }
    }
}
