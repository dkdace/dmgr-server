package com.dace.dmgr.event;

import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.event.listener.*;
import com.dace.dmgr.util.ReflectionUtil;
import lombok.experimental.UtilityClass;

/**
 * 이벤트 처리기를 등록하는 클래스.
 */
@UtilityClass
public final class EventListenerManager {
    static {
        ReflectionUtil.loadClass(OnAsyncPlayerChat.class);
        ReflectionUtil.loadClass(OnBlockBreak.class);
        ReflectionUtil.loadClass(OnBlockBurn.class);
        ReflectionUtil.loadClass(OnBlockFade.class);
        ReflectionUtil.loadClass(OnBlockPlace.class);
        ReflectionUtil.loadClass(OnChunkUnload.class);
        ReflectionUtil.loadClass(OnEntityDamage.class);
        ReflectionUtil.loadClass(OnEntityDamageByEntity.class);
        ReflectionUtil.loadClass(OnEntityDeath.class);
        ReflectionUtil.loadClass(OnEntityShootBowEvent.class);
        ReflectionUtil.loadClass(OnEntityTarget.class);
        ReflectionUtil.loadClass(OnFoodLevelChange.class);
        ReflectionUtil.loadClass(OnInventoryClick.class);
        ReflectionUtil.loadClass(OnInventoryClose.class);
        ReflectionUtil.loadClass(OnPlayerArmorStandManipulate.class);
        ReflectionUtil.loadClass(OnPlayerBucketEmpty.class);
        ReflectionUtil.loadClass(OnPlayerBucketFill.class);
        ReflectionUtil.loadClass(OnPlayerCommandPreprocess.class);
        ReflectionUtil.loadClass(OnPlayerDropItem.class);
        ReflectionUtil.loadClass(OnPlayerInteract.class);
        ReflectionUtil.loadClass(OnPlayerInteractEntity.class);
        ReflectionUtil.loadClass(OnPlayerItemHeld.class);
        ReflectionUtil.loadClass(OnPlayerJoin.class);
        ReflectionUtil.loadClass(OnPlayerMove.class);
        ReflectionUtil.loadClass(OnPlayerQuit.class);
        ReflectionUtil.loadClass(OnPlayerResourcePackStatus.class);
        ReflectionUtil.loadClass(OnPlayerSwapHandItems.class);
        ReflectionUtil.loadClass(OnPlayerToggleFlight.class);
        ReflectionUtil.loadClass(OnPlayerToggleSprint.class);
        ReflectionUtil.loadClass(OnTabComplete.class);
        ReflectionUtil.loadClass(OnWeatherChange.class);

        ReflectionUtil.loadClass(OnPlayClientUseEntity.class);
        ReflectionUtil.loadClass(OnPlayServerAbilities.class);
        ReflectionUtil.loadClass(OnPlayServerCustomSoundEffect.class);
        ReflectionUtil.loadClass(OnPlayServerNamedSoundEffect.class);
        ReflectionUtil.loadClass(OnPlayServerPlayerInfo.class);
        ReflectionUtil.loadClass(OnPlayServerUpdateHealth.class);

        ConsoleLogger.info("이벤트 등록 완료");
    }
}
