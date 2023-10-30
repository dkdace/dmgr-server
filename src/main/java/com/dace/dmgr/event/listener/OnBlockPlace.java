package com.dace.dmgr.event.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public final class OnBlockPlace implements Listener {
    @EventHandler
    public static void event(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (!player.isOp())
            event.setCancelled(true);
    }
}
