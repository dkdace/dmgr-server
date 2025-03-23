package com.dace.dmgr.event.listener;

import com.dace.dmgr.event.EventListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnPlayerBucketEmpty extends EventListener<PlayerBucketEmptyEvent> {
    @Getter
    private static final OnPlayerBucketEmpty instance = new OnPlayerBucketEmpty();

    @Override
    @EventHandler
    protected void onEvent(@NonNull PlayerBucketEmptyEvent event) {
        if (!event.getPlayer().isOp())
            event.setCancelled(true);
    }
}
