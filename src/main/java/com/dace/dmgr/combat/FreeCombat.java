package com.dace.dmgr.combat;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.game.map.GlobalLocation;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * 자유 전투 시스템 클래스.
 */
public final class FreeCombat {
    /** 대기실 위치 */
    private static final Location waitLocation = new Location(Bukkit.getWorld("DMGR"), -1089.5, 67, -114.5, 45, 0);
    /** 스폰 위치 목록 */
    private static final GlobalLocation[] spawnLocations = new GlobalLocation[]{
            new GlobalLocation(-817.5, 65, -132.5, -180, 0),
            new GlobalLocation(-893.5, 65, -136.5, 90, 0),
            new GlobalLocation(-919.5, 65, -191.5, 0, 0),
            new GlobalLocation(-989.5, 65, -194.5, 0, 0),
            new GlobalLocation(-951.5, 65, -98.5, -180, 0),
            new GlobalLocation(-861.5, 65, -100.5, -180, 0),
            new GlobalLocation(-861.5, 65, -100.5, -180, 0),
            new GlobalLocation(-817.5, 65, -89.5, -180, 0),
            new GlobalLocation(-817.5, 65, -89.5, -180, 0),
            new GlobalLocation(-821.5, 65, -58.5, -90, 0),
            new GlobalLocation(-889.5, 65, -73.5, 0, 0),
            new GlobalLocation(-943.5, 65, -39.5, 180, 0),
            new GlobalLocation(-1010.5, 65, -33.5, -90, 0),
            new GlobalLocation(-919.5, 65, 5.5, -180, 0),
            new GlobalLocation(-855.5, 65, -45.5, 0, 0),
            new GlobalLocation(-802.5, 65, 2.5, -180, 0),
            new GlobalLocation(-818.5, 65, -47.5, 0, 0),
            new GlobalLocation(-939.5, 65, -102.5, 0, 0),
            new GlobalLocation(-940.5, 65, -131.5, -90, 0),
            new GlobalLocation(-836.5, 65, -45.5, -90, 0),
            new GlobalLocation(-836.5, 73, -45.5, -90, 0),
            new GlobalLocation(-817.5, 78, -92.5, 180, 0),
            new GlobalLocation(-858.5, 85, -63.5, -90, 0),
            new GlobalLocation(-871.5, 84, -110.5, 0, 0),
            new GlobalLocation(-845.5, 80, -149.5, -90, 0),
            new GlobalLocation(-948.5, 77, -128.5, -180, 0),
            new GlobalLocation(-936.5, 88, -116.5, -90, 0),
            new GlobalLocation(-902.5, 87, -102.5, 180, 0),
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

        user.sendTitle("자유 전투", "§b§nF키§b를 눌러 전투원을 선택하십시오.", 10, 40, 30, 80);
        user.teleport(waitLocation);

        CombatUser combatUser = new CombatUser(user);

        TaskUtil.addTask(user, new IntervalTask(i -> {
            if (combatUser.getCharacterType() != null && LocationUtil.isInRegion(user.getPlayer(), "BattlePVPWarp")) {
                int index = DMGR.getRandom().nextInt(spawnLocations.length);
                user.teleport(spawnLocations[index].toLocation(Bukkit.getWorld("DMGR")));
            }

            return !combatUser.isDisposed();
        }, 4));
    }
}
