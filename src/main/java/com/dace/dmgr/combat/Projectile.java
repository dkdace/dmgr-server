package com.dace.dmgr.combat;

import com.dace.dmgr.combat.entity.ICombatEntity;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 투사체. 유한한 탄속을 가지는 총알을 관리하는 클래스.
 */
public abstract class Projectile extends Bullet {
    /** 투사체의 기본 판정 범위. 단위: 블록 */
    private static final float SIZE = 0.3F;
    /** 투사체의 속력 */
    protected int velocity;
    /** 중력의 영향을 받는지 여부 */
    protected boolean hasGravity;
    /**  */
    protected boolean bouncing;

    /**
     * 투사체를 생성한다.
     *
     * @param param 투사체 옵션
     * @see ProjectileParam
     */
    public Projectile(ProjectileParam param) {
        super(param.shooter, param.penetrating, param.trailInterval, param.hitboxMultiplier);
        this.velocity = param.velocity;
        this.hasGravity = param.hasGravity;
        this.bouncing = param.bouncing;
    }

    public void shoot(Location origin, Vector direction, float spread) {
        direction.normalize().multiply(HITBOX_INTERVAL);
        Location loc = origin.clone();
        direction = VectorUtil.getSpreadedVector(direction, spread);
        Set<ICombatEntity> targetList = new HashSet<>();

        Vector finalDirection = direction;

        new TaskTimer(1) {
            int count = 0;

            @Override
            public boolean run(int _i) {
                for (int i = 0; i < velocity / 5; i++) {
                    if (loc.distance(origin) >= MAX_RANGE)
                        return false;

                    Location hitLoc = loc.clone().add(finalDirection);
                    if (!LocationUtil.isNonSolid(hitLoc)) {
                        Vector subDir = finalDirection.clone().multiply(0.5);

                        while (LocationUtil.isNonSolid(loc))
                            loc.add(subDir);

                        loc.subtract(subDir);
                        onHit(loc);
                        onHitBlock(loc, hitLoc.getBlock());
                        return false;
                    }

                    if (loc.distance(origin) > 0.5) {
                        Map.Entry<ICombatEntity, Boolean> targetEntry
                                = Combat.getNearEnemy(shooter, loc, SIZE * hitboxMultiplier);

                        if (targetEntry != null) {
                            ICombatEntity target = targetEntry.getKey();
                            boolean isCrit = targetEntry.getValue();

                            if (!targetList.add(target)) {
                                onHit(hitLoc);
                                onHitEntity(hitLoc, target, isCrit);

                                if (!penetration)
                                    return false;
                            }
                        }
                    }
                    loc.add(finalDirection);
                    if (count++ % trailInterval == 0) trail(loc.clone());
                }

                return true;
            }
        };
    }
}
