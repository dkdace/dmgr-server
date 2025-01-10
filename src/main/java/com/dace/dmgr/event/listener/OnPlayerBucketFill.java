package com.dace.dmgr.event.listener;

import com.dace.dmgr.event.EventListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerBucketFillEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnPlayerBucketFill extends EventListener<PlayerBucketFillEvent> {
    @Getter
    private static final OnPlayerBucketFill instance = new OnPlayerBucketFill();

    @Override
    @EventHandler
    protected void onEvent(@NonNull PlayerBucketFillEvent event) {
        if (!event.getPlayer().isOp())
            event.setCancelled(true);
    }
}
