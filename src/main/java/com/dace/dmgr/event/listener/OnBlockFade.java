package com.dace.dmgr.event.listener;

import com.dace.dmgr.event.EventListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockFadeEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnBlockFade extends EventListener<BlockFadeEvent> {
    @Getter
    private static final OnBlockFade instance = new OnBlockFade();

    @Override
    @EventHandler
    protected void onEvent(@NonNull BlockFadeEvent event) {
        event.setCancelled(true);
    }
}
