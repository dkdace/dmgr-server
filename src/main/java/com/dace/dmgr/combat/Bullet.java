package com.dace.dmgr.combat;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.ICombatEntity;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.SoundPlayer;
import com.dace.dmgr.util.VectorUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public abstract class Bullet {
    private static final int MAX_RANGE = 70;
    private static final float HITBOX_INTERVAL = 0.25F;
    private final ICombatEntity shooter;
    private final int trailInterval;
    private final float hitboxMultiplier;

    public Bullet(ICombatEntity shooter, int trailInterval, float hitboxMultiplier) {
        this.shooter = shooter;
        this.trailInterval = trailInterval;
        this.hitboxMultiplier = hitboxMultiplier;
    }

    public Bullet(ICombatEntity shooter, int trailInterval) {
        this(shooter, trailInterval, 1);
    }

    public static void bulletHitEffect(Location location, Block hitBlock, boolean sound) {
        if (sound)
            SoundPlayer.play("random.gun.ricochet", location, 0.8F, (float) (0.975 + Math.random() * 0.05));

        location.getWorld().spawnParticle(Particle.BLOCK_DUST, location, 3, 0, 0, 0, 0.1, hitBlock.getState().getData());
        location.getWorld().spawnParticle(Particle.TOWN_AURA, location, 10, 0, 0, 0, 0);
    }

    public void shoot(Location origin, Vector direction, float spread) {
        direction.normalize().multiply(HITBOX_INTERVAL);
        Location loc = origin.clone();
        ICombatEntity target = null;
        direction = VectorUtil.spread(direction, spread);
        Vector subDir = direction.clone().multiply(0.5);

//        Location trailLoc = loc.clone().add(VectorUtil.getPitchAxis(loc).multiply(-0.2)).add(0, -0.2, 0);
//        origin.getWorld().spawnParticle(Particle.FLAME, trailLoc, 0, 0, 1, 0, 6);

        for (int i = 0; loc.distance(origin) < MAX_RANGE; i++) {
            Location hitLoc = loc.clone().add(direction);
            if (!LocationUtil.isNonSolid(hitLoc)) {
                while (LocationUtil.isNonSolid(loc))
                    loc.add(subDir);

                onHitBlock(loc.subtract(subDir), hitLoc.getBlock());
                break;
            }

            if (loc.distance(origin) > 0.5) {
                target = Combat.getNearEnemy(shooter, loc, Combat.HITBOX.HITSCAN * hitboxMultiplier);
                if (target != null) {
                    break;
                }
            }
            loc.add(direction);
            if (i % trailInterval == 0) trail(loc.clone());
        }

        if (target != null) {
            loc.add(direction);
            onHitEntity(loc.clone(), target);
        }
    }


    public void shoot(Location origin, float spread) {
        shoot(origin, shooter.getEntity().getLocation().getDirection(), spread);
    }

    public void shoot(Location origin) {
        shoot(origin, 0);
    }

    public void shoot(float spread) {
        if (shooter instanceof CombatUser)
            shoot(((CombatUser) shooter).getEntity().getEyeLocation(), spread);
        else
            shoot(shooter.getEntity().getLocation(), spread);
    }

    public void shoot() {
        if (shooter instanceof CombatUser)
            shoot(((CombatUser) shooter).getEntity().getEyeLocation());
        else
            shoot(shooter.getEntity().getLocation());
    }

    public abstract void trail(Location location);

    public void onHitBlock(Location location, Block hitBlock) {
        Bullet.bulletHitEffect(location, hitBlock, true);
    }

    public abstract void onHitEntity(Location location, ICombatEntity target);
}
