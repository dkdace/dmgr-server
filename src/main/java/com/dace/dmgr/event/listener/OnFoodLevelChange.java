package com.dace.dmgr.event.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public final class OnFoodLevelChange implements Listener {
    @EventHandler
    public static void event(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }
}
