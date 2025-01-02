package com.dace.dmgr.event.listener;

import com.dace.dmgr.user.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public final class OnPlayerCommandPreprocess implements Listener {
    @EventHandler
    public static void event(PlayerCommandPreprocessEvent event) {
        if (!User.fromPlayer(event.getPlayer()).onCommand())
            event.setCancelled(true);
    }
}
