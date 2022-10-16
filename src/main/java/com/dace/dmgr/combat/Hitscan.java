package com.dace.dmgr.combat;

import com.dace.dmgr.combat.entity.ICombatEntity;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public abstract class Hitscan extends Bullet {
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

                onHitBlock(loc.subtract(subDir), hitLoc.getBlock());
                break;
            }

            if (loc.distance(origin) > 0.5) {
                ICombatEntity target = Combat.getNearEnemy(shooter, loc, Combat.HITBOX.HITSCAN * hitboxMultiplier);

                if (target != null) {
                    if (!targetSet.add(target)) {
                        onHitEntity(hitLoc, target);

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
