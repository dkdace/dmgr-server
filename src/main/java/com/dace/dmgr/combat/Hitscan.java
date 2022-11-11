package com.dace.dmgr.combat;

import com.dace.dmgr.combat.entity.ICombatEntity;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class Hitscan extends Bullet {
    private static final float SIZE = 0.15F;

    public Hitscan(ICombatEntity shooter, boolean penetration, int trailInterval, float hitboxMultiplier) {
        super(shooter, penetration, trailInterval, hitboxMultiplier);
    }

    public Hitscan(ICombatEntity shooter, boolean penetration, int trailInterval) {
        super(shooter, penetration, trailInterval);
    }

    public void shoot(Location origin, Vector direction, float spread) {
        direction.normalize().multiply(HITBOX_INTERVAL);
        Location loc = origin.clone();
        direction = VectorUtil.spread(direction, spread);
        Set<ICombatEntity> targetSet = new HashSet<>();

//        Location trailLoc = loc.clone().add(VectorUtil.getPitchAxis(loc).multiply(-0.2)).add(0, -0.2, 0);
//        origin.getWorld().spawnParticle(Particle.FLAME, trailLoc, 0, 0, 1, 0, 6);

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

                        if (!penetration)
                            break;
                    }
                }
            }
            loc.add(direction);
            if (i % trailInterval == 0) trail(loc.clone());
        }
    }
}
