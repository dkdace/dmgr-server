package com.dace.dmgr.combat;

import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.Timespan;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.EntityUtil;
import com.dace.dmgr.util.LocationUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.RandomUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * 자유 전투 시스템 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FreeCombat {
    /** 자유 전투 설정 */
    private static final GeneralConfig.FreeCombatConfig CONFIG = GeneralConfig.getFreeCombatConfig();
    @Getter
    private static final FreeCombat instance = new FreeCombat();

    /**
     * 대기실 위치를 반환한다.
     *
     * @return 대기실 위치
     */
    @NonNull
    public Location getWaitLocation() {
        return CONFIG.getWaitLocation();
    }

    /**
     * 지정한 플레이어가 자유 전투 대기실 안에 있는지 확인한다.
     *
     * @param player 확인할 플레이어
     * @return 대기실 안에 있으면 {@code true} 반환
     */
    public boolean isInFreeCombatWait(@NonNull Player player) {
        return LocationUtil.isInRegion(player, CONFIG.getWaitRegionName());
    }

    /**
     * 지정한 플레이어가 자유 전투 이동 지역 안에 있는지 확인한다.
     *
     * @param player 확인할 플레이어
     * @return 이동 지역 안에 있으면 {@code true} 반환
     */
    public boolean isInFreeCombatWarp(@NonNull Player player) {
        return LocationUtil.isInRegion(player, CONFIG.getWarpRegionName());
    }

    /**
     * 플레이어가 자유 전투를 시작했을 때 실행할 작업.
     *
     * @param user 대상 플레이어
     */
    public void onStart(@NonNull User user) {
        user.sendTitle("자유 전투", "§b§nF키§b를 눌러 전투원을 선택하십시오.", Timespan.ofSeconds(0.5), Timespan.ofSeconds(2),
                Timespan.ofSeconds(1.5), Timespan.ofSeconds(4));
        EntityUtil.teleport(user.getPlayer(), getWaitLocation());
    }

    /**
     * 플레이어를 자유 전투 전장의 무작위 위치로 이동시킨다.
     *
     * @param player 이동할 플레이어
     */
    public void teleportRandom(@NonNull Player player) {
        EntityUtil.teleport(player, CONFIG.getSpawnLocations()[RandomUtils.nextInt(0, CONFIG.getSpawnLocations().length)]);
    }
}
