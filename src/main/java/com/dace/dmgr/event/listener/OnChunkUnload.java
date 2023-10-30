package com.dace.dmgr.event.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

public final class OnChunkUnload implements Listener {
    @EventHandler
    public static void event(ChunkUnloadEvent event) {
        event.setCancelled(true);
    }
}
