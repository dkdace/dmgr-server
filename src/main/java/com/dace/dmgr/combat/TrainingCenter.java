package com.dace.dmgr.combat;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * 훈련장 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TrainingCenter {
    @Getter
    private static final TrainingCenter instance = new TrainingCenter();

    /** 훈련장 월드 인스턴스 */
    private static final World WORLD = Bukkit.getWorld("Training");
    /** 스폰 위치 */
    private static final Location SPAWN_LOCATION = new Location(WORLD, 95.5, 220, 111.5, 270, 0);

    /**
     * 스폰 위치를 반환한다.
     *
     * @return 스폰 위치
     */
    @NonNull
    public Location getSpawnLocation() {
        return SPAWN_LOCATION.clone();
    }

    /**
     * 플레이어가 훈련장을 시작했을 때 실행할 작업.
     *
     * @param user 대상 플레이어
     */
    public void onStart(@NonNull User user) {
        user.sendTitle("훈련장", "§b신호기 위치에서 전투원을 선택할 수 있습니다.", Timespan.ofSeconds(0.5), Timespan.ofSeconds(2),
                Timespan.ofSeconds(1.5), Timespan.ofSeconds(4));
        user.teleport(SPAWN_LOCATION);
    }
}
