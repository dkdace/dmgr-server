package com.dace.dmgr.event.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

public final class OnPlayerBucketEmpty implements Listener {
    @EventHandler
    public static void event(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();

        if (!player.isOp())
            event.setCancelled(true);
    }
}
