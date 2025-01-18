package com.dace.dmgr.event;

import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.event.listener.*;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.Validate;

/**
 * 이벤트 처리기를 등록하는 클래스.
 */
@UtilityClass
public final class EventListenerManager {
    /**
     * 모든 이벤트 처리기를 등록한다.
     *
     * <p>플러그인 활성화 시 호출해야 한다.</p>
     */
    public static void register() {
        Validate.notNull(OnAsyncPlayerChat.getInstance());
        Validate.notNull(OnBlockBreak.getInstance());
        Validate.notNull(OnBlockBurn.getInstance());
        Validate.notNull(OnBlockFade.getInstance());
        Validate.notNull(OnBlockPlace.getInstance());
        Validate.notNull(OnChunkUnload.getInstance());
        Validate.notNull(OnEntityDamage.getInstance());
        Validate.notNull(OnEntityDamageByEntity.getInstance());
        Validate.notNull(OnEntityDeath.getInstance());
        Validate.notNull(OnEntityShootBowEvent.getInstance());
        Validate.notNull(OnEntityTarget.getInstance());
        Validate.notNull(OnFoodLevelChange.getInstance());
        Validate.notNull(OnInventoryClick.getInstance());
        Validate.notNull(OnInventoryClose.getInstance());
        Validate.notNull(OnPlayerArmorStandManipulate.getInstance());
        Validate.notNull(OnPlayerBucketEmpty.getInstance());
        Validate.notNull(OnPlayerBucketFill.getInstance());
        Validate.notNull(OnPlayerCommandPreprocess.getInstance());
        Validate.notNull(OnPlayerDropItem.getInstance());
        Validate.notNull(OnPlayerInteract.getInstance());
        Validate.notNull(OnPlayerInteractEntity.getInstance());
        Validate.notNull(OnPlayerItemHeld.getInstance());
        Validate.notNull(OnPlayerJoin.getInstance());
        Validate.notNull(OnPlayerQuit.getInstance());
        Validate.notNull(OnPlayerResourcePackStatus.getInstance());
        Validate.notNull(OnPlayerSwapHandItems.getInstance());
        Validate.notNull(OnPlayerToggleFlight.getInstance());
        Validate.notNull(OnPlayerToggleSprint.getInstance());
        Validate.notNull(OnTabComplete.getInstance());
        Validate.notNull(OnWeatherChange.getInstance());

        Validate.notNull(OnPlayClientUseEntity.getInstance());
        Validate.notNull(OnPlayServerAbilities.getInstance());
        Validate.notNull(OnPlayServerCustomSoundEffect.getInstance());
        Validate.notNull(OnPlayServerNamedSoundEffect.getInstance());
        Validate.notNull(OnPlayServerPlayerInfo.getInstance());
        Validate.notNull(OnPlayServerUpdateHealth.getInstance());

        ConsoleLogger.info("이벤트 등록 완료");
    }
}
