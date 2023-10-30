package com.dace.dmgr.event.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public final class OnBlockBreak implements Listener {
    @EventHandler
    public static void event(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (!player.isOp())
            event.setCancelled(true);
    }
}
