package com.dace.dmgr.combat;

import com.dace.dmgr.GlobalLocation;
import com.dace.dmgr.Timespan;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.EntityUtil;
import com.dace.dmgr.util.LocationUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * 자유 전투 시스템 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FreeCombat {
    @Getter
    private static final FreeCombat instance = new FreeCombat();

    /** 자유 전투 대기실 지역 이름 */
    private static final String WAIT_REGION_NAME = "BattlePVP";
    /** 자유 전투 이동 지역 이름 */
    private static final String WARP_REGION_NAME = "BattlePVPWarp";
    /** 자유 전투 월드 인스턴스 */
    private static final World WORLD = Bukkit.getWorld("FreeCombat");
    /** 대기실 위치 */
    private static final Location WAIT_LOCATION = new Location(WORLD, 18.5, 29, 16, 90, 0);
    /** 스폰 위치 목록 */
    private static final GlobalLocation[] SPAWN_LOCATIONS = {
            new GlobalLocation(89.5, 66, -22.5, -180, 0),
            new GlobalLocation(13.5, 66, -26.5, 90, 0),
            new GlobalLocation(-12.5, 66, -81.5, 0, 0),
            new GlobalLocation(-82.5, 66, -84.5, 0, 0),
            new GlobalLocation(-44.5, 66, 11.5, -180, 0),
            new GlobalLocation(45.5, 66, 9.5, -180, 0),
            new GlobalLocation(89.5, 66, 20.5, -180, 0),
            new GlobalLocation(85.5, 66, 51.5, -90, 0),
            new GlobalLocation(17.5, 66, 36.5, 0, 0),
            new GlobalLocation(-36.5, 66, 70.5, 180, 0),
            new GlobalLocation(-103.5, 66, 76.5, -90, 0),
            new GlobalLocation(-12.5, 66, 115.5, -180, 0),
            new GlobalLocation(51.5, 66, 64.5, 0, 0),
            new GlobalLocation(104.5, 66, 112.5, -180, 0),
            new GlobalLocation(88.5, 66, 62.5, 0, 0),
            new GlobalLocation(-32.5, 66, 7.5, 0, 0),
            new GlobalLocation(-33.5, 66, -21.5, -90, 0),
            new GlobalLocation(70.5, 74, 64.5, -90, 0),
            new GlobalLocation(89.5, 79, 17.5, 180, 0),
            new GlobalLocation(48.5, 86, 46.5, -90, 0),
            new GlobalLocation(35.5, 85, -0.5, 0, 0),
            new GlobalLocation(61.5, 82, -39.5, -90, 0),
            new GlobalLocation(-41.5, 79, -18.5, -180, 0),
            new GlobalLocation(-29.5, 89, -6.5, -90, 0),
            new GlobalLocation(4.5, 88, 7.5, 180, 0),
    };

    /**
     * 대기실 위치를 반환한다.
     *
     * @return 대기실 위치
     */
    @NonNull
    public Location getWaitLocation() {
        return WAIT_LOCATION.clone();
    }

    /**
     * 지정한 플레이어가 자유 전투 대기실 안에 있는지 확인한다.
     *
     * @param player 확인할 플레이어
     * @return 대기실 안에 있으면 {@code true} 반환
     */
    public boolean isInFreeCombatWait(@NonNull Player player) {
        return LocationUtil.isInRegion(player, WAIT_REGION_NAME);
    }

    /**
     * 지정한 플레이어가 자유 전투 이동 지역 안에 있는지 확인한다.
     *
     * @param player 확인할 플레이어
     * @return 이동 지역 안에 있으면 {@code true} 반환
     */
    public boolean isInFreeCombatWarp(@NonNull Player player) {
        return LocationUtil.isInRegion(player, WARP_REGION_NAME);
    }

    /**
     * 플레이어가 자유 전투를 시작했을 때 실행할 작업.
     *
     * @param user 대상 플레이어
     */
    public void onStart(@NonNull User user) {
        user.sendTitle("자유 전투", "§b§nF키§b를 눌러 전투원을 선택하십시오.", Timespan.ofSeconds(0.5), Timespan.ofSeconds(2),
                Timespan.ofSeconds(1.5), Timespan.ofSeconds(4));
        EntityUtil.teleport(user.getPlayer(), WAIT_LOCATION);
    }

    /**
     * 플레이어를 자유 전투 전장의 무작위 위치로 이동시킨다.
     *
     * @param player 이동할 플레이어
     */
    public void teleportRandom(@NonNull Player player) {
        EntityUtil.teleport(player, SPAWN_LOCATIONS[RandomUtils.nextInt(0, SPAWN_LOCATIONS.length)].toLocation(WORLD));
    }
}
