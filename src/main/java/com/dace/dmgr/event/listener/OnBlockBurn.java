package com.dace.dmgr.event.listener;

import com.dace.dmgr.event.EventListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBurnEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnBlockBurn extends EventListener<BlockBurnEvent> {
    @Getter
    private static final OnBlockBurn instance = new OnBlockBurn();

    @Override
    @EventHandler
    protected void onEvent(@NonNull BlockBurnEvent event) {
        event.setCancelled(true);
    }
}
