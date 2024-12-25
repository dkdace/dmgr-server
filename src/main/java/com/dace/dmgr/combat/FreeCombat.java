package com.dace.dmgr.combat;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.GlobalLocation;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.Timespan;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * 자유 전투 시스템 클래스.
 */
@UtilityClass
public final class FreeCombat {
    /** 자유 전투 지역 이름 */
    public static final String FREE_COMBAT_REGION = "BattlePVP";
    /** 자유 전투 월드 객체 */
    private static final World world = Bukkit.getWorld("FreeCombat");
    /** 대기실 위치 */
    private static final Location waitLocation = new Location(world, 18.5, 29, 16, 90, 0);
    /** 자유 전투 이동 지역 이름 */
    private static final String FREE_COMBAT_WARP_REGION = "BattlePVPWarp";
    /** 스폰 위치 목록 */
    private static final GlobalLocation[] spawnLocations = new GlobalLocation[]{
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
    public static Location getWaitLocation() {
        return waitLocation.clone();
    }

    /**
     * 대상 플레이어의 자유 전투를 시작한다.
     *
     * @param user 대상 플레이어
     */
    public static void start(@NonNull User user) {
        if (CombatUser.fromUser(user) != null)
            return;

        user.sendTitle("자유 전투", "§b§nF키§b를 눌러 전투원을 선택하십시오.", Timespan.ofTicks(10), Timespan.ofTicks(40), Timespan.ofTicks(30),
                Timespan.ofTicks(80));
        user.teleport(waitLocation);
        user.setInFreeCombat(true);

        CombatUser combatUser = new CombatUser(user);

        TaskUtil.addTask(user, new IntervalTask(i -> {
            if (combatUser.getCharacterType() != null && LocationUtil.isInRegion(user.getPlayer(), FREE_COMBAT_WARP_REGION)) {
                int index = DMGR.getRandom().nextInt(spawnLocations.length);
                user.teleport(spawnLocations[index].toLocation(world));
            }

            return !combatUser.isDisposed();
        }, 4));
    }
}
