package com.dace.dmgr.event.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Sittable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public final class OnPlayerInteractEntity implements Listener {
    @EventHandler
    public static void event(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();

        if (entity instanceof Sittable)
            event.setCancelled(true);
    }
}
