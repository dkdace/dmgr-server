package com.dace.dmgr.event.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketFillEvent;

public final class OnPlayerBucketFill implements Listener {
    @EventHandler
    public static void event(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();

        if (!player.isOp())
            event.setCancelled(true);
    }
}
