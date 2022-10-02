package com.dace.dmgr.event.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;

public class OnBlockBurn implements Listener {
    @EventHandler
    public static void event(BlockBurnEvent event) {
        event.setCancelled(true);
    }
}
