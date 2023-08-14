package com.dace.dmgr.event.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;

public final class OnBlockFade implements Listener {
    @EventHandler
    public static void event(BlockFadeEvent event) {
        event.setCancelled(true);
    }
}
