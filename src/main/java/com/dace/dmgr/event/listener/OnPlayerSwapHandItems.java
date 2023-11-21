package com.dace.dmgr.event.listener;

import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.gui.SelectChar;
import com.dace.dmgr.system.EntityInfoRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public final class OnPlayerSwapHandItems implements Listener {
    @EventHandler
    public static void event(PlayerSwapHandItemsEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        GameUser gameUser = EntityInfoRegistry.getGameUser(event.getPlayer());

        if (gameUser != null && gameUser.isInSpawnRegion()) {
            event.setCancelled(true);
            SelectChar.getInstance().open(player);
        }
    }
}
