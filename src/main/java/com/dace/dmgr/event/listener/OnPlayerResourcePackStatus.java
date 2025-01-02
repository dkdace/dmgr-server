package com.dace.dmgr.event.listener;

import com.dace.dmgr.user.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public final class OnPlayerResourcePackStatus implements Listener {
    @EventHandler
    public static void event(PlayerResourcePackStatusEvent event) {
        User.fromPlayer(event.getPlayer()).onResourcePackStatus(event.getStatus());
    }
}
