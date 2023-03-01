package com.dace.dmgr.combat;

import com.dace.dmgr.combat.entity.ICombatEntity;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 히트스캔. 광선과 같이 탄속이 무한한 총알을 관리하는 클래스.
 */
public abstract class Hitscan extends Bullet {
    /** 히트스캔의 기본 판정 범위. 단위: 블록 */
    private static final float SIZE = 0.15F;

    /**
     * 히트스캔 인스턴스를 생성한다.
     */
    public Hitscan(ICombatEntity shooter, int trailInterval, HitscanOption option) {
        super(shooter, trailInterval, option.penetrating, option.hitboxMultiplier);
    }

    /**
     * 히트스캔 인스턴스를 생성한다.
     */
    public Hitscan(ICombatEntity shooter, int trailInterval) {
        super(shooter, trailInterval);
    }

    /**
     * 히트스캔 총알을 발사한다.
     *
     * @param origin    발화점
     * @param direction 발사 방향
     * @param spread    탄퍼짐 정도
     */
    public void shoot(Location origin, Vector direction, float spread) {
        direction.normalize().multiply(HITBOX_INTERVAL);
        Location loc = origin.clone();
        direction = VectorUtil.getSpreadedVector(direction, spread);
        Set<ICombatEntity> targetSet = new HashSet<>();

        for (int i = 0; loc.distance(origin) < MAX_RANGE; i++) {
            Location hitLoc = loc.clone().add(direction);
            if (!LocationUtil.isNonSolid(hitLoc)) {
                Vector subDir = direction.clone().multiply(0.5);

                while (LocationUtil.isNonSolid(loc))
                    loc.add(subDir);

                loc.subtract(subDir);
                onHit(loc);
                onHitBlock(loc, hitLoc.getBlock());
                break;
            }

            if (loc.distance(origin) > 0.5) {
                Map.Entry<ICombatEntity, Boolean> targetEntry
                        = Combat.getNearEnemy(shooter, loc, SIZE * hitboxMultiplier);
                ICombatEntity target = targetEntry.getKey();
                boolean isCrit = targetEntry.getValue();

                if (target != null) {
                    if (!targetSet.add(target)) {
                        onHit(hitLoc);
                        onHitEntity(hitLoc, target, isCrit);

                        if (!penetrating)
                            break;
                    }
                }
            }
            loc.add(direction);
            if (i % trailInterval == 0) trail(loc.clone());
        }
    }
}
