package com.dace.dmgr.event.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public final class OnPlayerSwapHandItems implements Listener {
    @EventHandler
    public static void event(PlayerSwapHandItemsEvent event) {
        event.setCancelled(true);
    }
}
