package com.dace.dmgr.event;

import com.dace.dmgr.event.listener.*;
import com.dace.dmgr.gui.menu.ChatSoundOption;
import com.dace.dmgr.gui.menu.Menu;
import com.dace.dmgr.gui.menu.PlayerOption;

/**
 * 메인 이벤트를 등록하는 클래스.
 */
public final class MainEventManager {
    public static void init() {
        EventUtil.registerListener(new OnPlayerJoin());
        EventUtil.registerListener(new OnPlayerQuit());
        EventUtil.registerListener(new OnPlayerResourcePackStatus());
        EventUtil.registerListener(new OnAsyncPlayerChat());
        EventUtil.registerListener(new OnBlockBreak());
        EventUtil.registerListener(new OnBlockPlace());
        EventUtil.registerListener(new OnInventoryClick());
        EventUtil.registerListener(new OnBlockFade());
        EventUtil.registerListener(new OnBlockBurn());
        EventUtil.registerListener(new OnWeatherChange());
        EventUtil.registerListener(new OnFoodLevelChange());
        EventUtil.registerListener(new OnEntityDamage());
        EventUtil.registerListener(new OnEntityDamageByEntity());
        EventUtil.registerListener(new OnEntityDeath());
        EventUtil.registerListener(new OnPlayerSwapHandItems());
        EventUtil.registerListener(new OnPlayerToggleSprint());
        EventUtil.registerListener(new OnPlayerDropItem());
        EventUtil.registerListener(new OnTabComplete());
        EventUtil.registerListener(new OnPlayerCommandPreprocess());
        EventUtil.registerListener(new OnWeaponPreShoot());
        EventUtil.registerListener(new OnWeaponPrepareShoot());
        EventUtil.registerListener(new OnPlayerItemHeld());
        EventUtil.registerListener(new OnPlayerInteract());
        EventUtil.registerListener(new Menu());
        EventUtil.registerListener(new PlayerOption());
        EventUtil.registerListener(new ChatSoundOption());
        EventUtil.registerPacketListener(new OnPlayServerUpdateHealth());
        EventUtil.registerPacketListener(new OnPlayClientArmAnimation());
        EventUtil.registerPacketListener(new OnPlayServerNamedSoundEffect());
    }
}
