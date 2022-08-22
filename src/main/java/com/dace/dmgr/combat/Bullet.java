package com.dace.dmgr.combat;

import com.dace.dmgr.combat.entity.ICombatEntity;
import com.dace.dmgr.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public abstract class Bullet {
    private static final int MAX_RANGE = 70;
    private final ICombatEntity shooter;
    private final String name;

    public Bullet(ICombatEntity shooter, String name) {
        this.shooter = shooter;
        this.name = name;
    }

    public void shoot(Location origin, Vector direction) {
        Location loc = origin.clone();
        ICombatEntity target = null;

        for (int i = 0; loc.distance(origin) < MAX_RANGE; i++) {
            if (!LocationUtil.isNonSolid(loc)) {
                onHitBlock(loc);
                break;
            }

            if (loc.distance(origin) > 0.5) {
//                target = Combat.getNearEnemy(shooter, loc, Combat.HITS_HITBOX);
                if (target != null) {
                    break;
                }
            }
            loc.add(direction);
            if (i % 7 == 0) trail(loc);
        }

        if (target != null) {
            loc.add(direction);
            onHitEntity(loc, target);
        }
    }

    public abstract void trail(Location location);

    public abstract void onHitBlock(Location location);

    public abstract void onHitEntity(Location location, ICombatEntity target);
}
