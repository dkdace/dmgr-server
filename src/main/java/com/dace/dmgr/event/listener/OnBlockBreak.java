package com.dace.dmgr.event.listener;

import com.dace.dmgr.event.EventListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnBlockBreak extends EventListener<BlockBreakEvent> {
    @Getter
    private static final OnBlockBreak instance = new OnBlockBreak();

    @Override
    @EventHandler
    protected void onEvent(@NonNull BlockBreakEvent event) {
        if (!event.getPlayer().isOp())
            event.setCancelled(true);
    }
}
