package com.dace.dmgr.event;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.event.listener.*;
import org.bukkit.event.Listener;

public abstract class EventManager {
    private static void registerEvent(Listener listener) {
        DMGR.getPlugin().getServer().getPluginManager().registerEvents(listener, DMGR.getPlugin());
    }

    public static void init() {
        registerEvent(new OnPlayerJoin());
        registerEvent(new OnPlayerQuit());
        registerEvent(new OnPlayerResourcePackStatus());
        registerEvent(new OnAsyncPlayerChat());
        registerEvent(new OnBlockBreak());
        registerEvent(new OnBlockPlace());
        registerEvent(new OnInventoryClick());
        registerEvent(new OnBlockFade());
        registerEvent(new OnBlockBurn());
        registerEvent(new OnWeatherChange());
        registerEvent(new OnFoodLevelChange());
        registerEvent(new OnEntityDamage());
        registerEvent(new OnEntityDamageByEntity());
        registerEvent(new OnEntityDeath());
        registerEvent(new OnPlayerSwapHandItems());
        registerEvent(new OnPlayerToggleSprint());
        registerEvent(new OnTabComplete());
        registerEvent(new OnPlayerCommandPreprocess());
        registerEvent(new OnWeaponPreShoot());
        registerEvent(new OnPlayerItemHeld());
        registerEvent(new OnPlayerInteract());
    }
}
