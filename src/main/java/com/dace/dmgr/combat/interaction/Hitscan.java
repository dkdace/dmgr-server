package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.entity.CombatEntity;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * 히트스캔. 광선과 같이 탄속이 무한한 총알을 관리하는 클래스.
 */
public abstract class Hitscan extends Bullet {
    /**
     * 히트스캔 인스턴스를 생성한다.
     *
     * <p>히트스캔의 선택적 옵션은 {@link HitscanOption} 객체를 통해 전달받는다.</p>
     *
     * @param shooter 발사자
     * @param option  선택적 옵션
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @see HitscanOption
     */
    protected Hitscan(@NonNull CombatEntity shooter, @NonNull HitscanOption option) {
        super(shooter, option.trailInterval, option.startDistance, option.maxDistance, option.size, option.condition);
    }

    /**
     * 히트스캔 인스턴스를 생성한다.
     *
     * @param shooter 발사자
     */
    protected Hitscan(@NonNull CombatEntity shooter) {
        super(shooter, HitscanOption.TRAIL_INTERVAL_DEFAULT, HitscanOption.START_DISTANCE_DEFAULT, HitscanOption.MAX_DISTANCE_DEFAULT,
                HitscanOption.SIZE_DEFAULT, HitscanOption.CONDITION_DEFAULT);
    }

    @Override
    protected final void onShoot(@NonNull Location origin, @NonNull Vector direction) {
        for (int i = 0; getLocation().distance(origin) < maxDistance; i++) {
            if (!onInterval())
                break;

            if (getVelocity().length() > 0.01)
                getLocation().add(getVelocity());
            if (i % trailInterval == 0)
                trail();
        }

        onDestroy();
    }
}
