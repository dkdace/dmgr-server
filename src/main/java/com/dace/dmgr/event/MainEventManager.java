package com.dace.dmgr.event;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.dace.dmgr.event.listener.*;

public class MainEventManager extends EventManager {
    private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    public static void init() {
        registerListener(new OnPlayerJoin());
        registerListener(new OnPlayerQuit());
        registerListener(new OnPlayerResourcePackStatus());
        registerListener(new OnAsyncPlayerChat());
        registerListener(new OnBlockBreak());
        registerListener(new OnBlockPlace());
        registerListener(new OnInventoryClick());
        registerListener(new OnBlockFade());
        registerListener(new OnBlockBurn());
        registerListener(new OnWeatherChange());
        registerListener(new OnFoodLevelChange());
        registerListener(new OnEntityDamage());
        registerListener(new OnEntityDamageByEntity());
        registerListener(new OnEntityDeath());
        registerListener(new OnPlayerSwapHandItems());
        registerListener(new OnPlayerToggleSprint());
        registerListener(new OnPlayerDropItem());
        registerListener(new OnTabComplete());
        registerListener(new OnPlayerCommandPreprocess());
        registerListener(new OnWeaponPreShoot());
        registerListener(new OnWeaponPrepareShoot());
        registerListener(new OnPlayerItemHeld());
        registerListener(new OnPlayerInteract());
        registerPacketListener(new OnPlayServerUpdateHealth());
        registerPacketListener(new OnPlayClientArmAnimation());
    }
}
