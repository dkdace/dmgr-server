package com.dace.dmgr.event.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class OnPlayerCommandPreprocess implements Listener {
    @EventHandler
    public static void event(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().equals("spawn"))
            event.setCancelled(true);
    }
}
