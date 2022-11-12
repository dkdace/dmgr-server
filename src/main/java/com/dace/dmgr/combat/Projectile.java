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

public abstract class Projectile extends Bullet {
    private static final float SIZE = 0.3F;
    protected int velocity;
    protected boolean hasGravity;
    protected boolean bouncing;

    /**
     * {@code Projectile}을 생성할 때는 {@link ProjectileParam.Builder}를 이용해 얻은 객체를 전달해주세요.
     * @param param {@link ProjectileParam} 객체
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
        direction = VectorUtil.spread(direction, spread);
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
                        ICombatEntity target = targetEntry.getKey();
                        boolean isCrit = targetEntry.getValue();

                        if (target != null) {
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
