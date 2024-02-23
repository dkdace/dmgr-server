package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.util.LocationUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

/**
 * 히트스캔. 광선과 같이 탄속이 무한한 총알을 관리하는 클래스.
 */
public abstract class Hitscan extends Bullet {
    /** 기본 판정 범위. (단위: 블록) */
    private static final double SIZE = 0.05;

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
        super(shooter, option.trailInterval, option.maxDistance, option.hitboxMultiplier, option.condition);
    }

    /**
     * 히트스캔 인스턴스를 생성한다.
     *
     * @param shooter 발사자
     */
    protected Hitscan(@NonNull CombatEntity shooter) {
        super(shooter);
        HitscanOption hitscanOption = HitscanOption.builder().build();
        this.trailInterval = hitscanOption.trailInterval;
        this.maxDistance = hitscanOption.maxDistance;
        this.hitboxMultiplier = hitscanOption.hitboxMultiplier;
        this.condition = hitscanOption.condition;
    }

    /**
     * 히트스캔 총알을 발사한다.
     *
     * @param origin    발사 위치
     * @param direction 발사 방향
     */
    @Override
    public final void shoot(@NonNull Location origin, @NonNull Vector direction) {
        direction.normalize().multiply(HITBOX_INTERVAL);
        Location loc = origin.clone();
        loc.add(direction.clone().multiply(START_DISTANCE));
        Set<CombatEntity> targets = new HashSet<>();

        for (int i = 0; loc.distance(origin) < maxDistance; i++) {
            if (!LocationUtil.isNonSolid(loc) && !handleBlockCollision(loc, direction))
                break;

            if (!findTargetAndHandleCollision(loc, direction, targets, SIZE, condition))
                break;

            loc.add(direction);
            if (i % trailInterval == 0)
                trail(loc.clone());
        }

        onDestroy(loc.clone());
    }
}
