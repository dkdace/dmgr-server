package com.dace.dmgr.event.listener;

import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.item.gui.SelectChar;
import com.dace.dmgr.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public final class OnPlayerSwapHandItems implements Listener {
    @EventHandler
    public static void event(PlayerSwapHandItemsEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        User user = User.fromPlayer(player);
        GameUser gameUser = GameUser.fromUser(user);

        if (gameUser != null && gameUser.getSpawnRegionTeam() == gameUser.getTeam()) {
            event.setCancelled(true);
            SelectChar.getInstance().open(player);
        }
    }
}
