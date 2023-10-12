package com.dace.dmgr.combat;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

/**
 * 히트스캔. 광선과 같이 탄속이 무한한 총알을 관리하는 클래스.
 */
public abstract class Hitscan extends Bullet {
    /** 히트스캔의 기본 판정 범위. 단위: 블록 */
    private static final float SIZE = 0.05F;

    /**
     * 히트스캔 인스턴스를 생성한다.
     *
     * <p>히트스캔의 선택적 옵션은 {@link HitscanOption} 객체를 통해 전달받는다.</p>
     *
     * @param shooter 발사하는 엔티티
     * @param option  선택적 옵션
     * @see HitscanOption
     */
    protected Hitscan(CombatEntity<?> shooter, HitscanOption option) {
        super(shooter, option.trailInterval, option.maxDistance, option.penetrating, option.hitboxMultiplier);
    }

    /**
     * 히트스캔 인스턴스를 생성한다.
     *
     * @param shooter 발사하는 엔티티
     */
    protected Hitscan(CombatEntity<?> shooter) {
        super(shooter);
        HitscanOption hitscanOption = HitscanOption.builder().build();
        this.trailInterval = hitscanOption.trailInterval;
        this.maxDistance = hitscanOption.maxDistance;
        this.penetrating = hitscanOption.penetrating;
        this.hitboxMultiplier = hitscanOption.hitboxMultiplier;
    }

    /**
     * 히트스캔 총알을 발사한다.
     *
     * @param origin    발화점
     * @param direction 발사 방향
     * @param spread    탄퍼짐 정도. 단위: ×0.02블록/블록
     */
    @Override
    public final void shoot(Location origin, Vector direction, float spread) {
        direction.normalize().multiply(HITBOX_INTERVAL);
        Location loc = origin.clone();
        direction = VectorUtil.getSpreadedVector(direction, spread);
        Set<CombatEntity<?>> targets = new HashSet<>();

        for (int i = 0; loc.distance(origin) < maxDistance; i++) {
            if (!LocationUtil.isNonSolid(loc) && !handleBlockCollision(loc, direction))
                break;

            if (loc.distance(origin) > MIN_DISTANCE && !findEnemyAndHandleCollision(loc, direction, targets, SIZE))
                break;

            loc.add(direction);
            if (i % trailInterval == 0)
                trail(loc.clone());
        }

        onDestroy(loc.clone());
    }
}
