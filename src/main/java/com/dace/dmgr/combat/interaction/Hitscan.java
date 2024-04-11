package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.util.LocationUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.HashSet;

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

    /**
     * 히트스캔 총알을 발사한다.
     *
     * @param origin    발사 위치
     * @param direction 발사 방향
     */
    @Override
    public final void shoot(@NonNull Location origin, @NonNull Vector direction) {
        velocity = direction.clone().normalize().multiply(HITBOX_INTERVAL);
        location = origin.clone();
        location.add(direction.clone().multiply(startDistance));
        HashSet<Damageable> targets = new HashSet<>();

        for (int i = 0; location.distance(origin) < maxDistance; i++) {
            if (!onInterval())
                break;

            if (!LocationUtil.isNonSolid(location) && !handleBlockCollision())
                break;

            if (!findTargetAndHandleCollision(targets, condition))
                break;

            location.add(velocity);
            if (i % trailInterval == 0)
                trail();
        }

        onDestroy();
    }

    @Override
    protected boolean onInterval() {
        return true;
    }
}
