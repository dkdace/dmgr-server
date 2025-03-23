package com.dace.dmgr.event.listener;

import com.dace.dmgr.event.EventListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.ChunkUnloadEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnChunkUnload extends EventListener<ChunkUnloadEvent> {
    @Getter
    private static final OnChunkUnload instance = new OnChunkUnload();

    @Override
    @EventHandler(priority = EventPriority.HIGH)
    protected void onEvent(@NonNull ChunkUnloadEvent event) {
        event.setCancelled(true);
    }
}
