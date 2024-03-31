package com.dace.dmgr.event;

import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.event.listener.*;
import lombok.experimental.UtilityClass;

/**
 * 서버 이벤트를 등록하는 클래스.
 */
@UtilityClass
public final class EventManager {
    /**
     * 이벤트를 등록한다.
     */
    public static void register() {
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
        EventUtil.registerListener(new OnChunkUnload());
        EventUtil.registerListener(new OnFoodLevelChange());
        EventUtil.registerListener(new OnEntityDamage());
        EventUtil.registerListener(new OnEntityDamageByEntity());
        EventUtil.registerListener(new OnEntityDeath());
        EventUtil.registerListener(new OnEntityTarget());
        EventUtil.registerListener(new OnPlayerSwapHandItems());
        EventUtil.registerListener(new OnPlayerToggleSprint());
        EventUtil.registerListener(new OnPlayerToggleFlight());
        EventUtil.registerListener(new OnPlayerDropItem());
        EventUtil.registerListener(new OnTabComplete());
        EventUtil.registerListener(new OnPlayerCommandPreprocess());
        EventUtil.registerListener(new OnPlayerItemHeld());
        EventUtil.registerListener(new OnPlayerBucketEmpty());
        EventUtil.registerListener(new OnPlayerBucketFill());
        EventUtil.registerListener(new OnPlayerInteract());
        EventUtil.registerListener(new OnPlayerInteractEntity());

        EventUtil.registerPacketListener(new OnPlayClientUseEntity());
        EventUtil.registerPacketListener(new OnPlayServerUpdateHealth());
        EventUtil.registerPacketListener(new OnPlayServerNamedSoundEffect());
//        EventUtil.registerPacketListener(new OnPlayClientLook());
//        EventUtil.registerPacketListener(new OnPlayClientPositionLook());
        EventUtil.registerPacketListener(new OnPlayServerEntityMetadata());
        EventUtil.registerPacketListener(new OnPlayServerPlayerInfo());

        ConsoleLogger.info("이벤트 등록 완료");
    }
}
