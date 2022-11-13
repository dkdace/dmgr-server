package com.dace.dmgr.combat;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.ICombatEntity;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public abstract class Bullet {
    protected static final int MAX_RANGE = 70;
    protected static final float HITBOX_INTERVAL = 0.25F;
    protected static final int TRAIL_INTERVAL = 7;
    protected final ICombatEntity shooter;
    protected final int trailInterval;
    protected final float hitboxMultiplier;
    protected final boolean penetration;

    public Bullet(ICombatEntity shooter, boolean penetration, int trailInterval, float hitboxMultiplier) {
        this.shooter = shooter;
        this.trailInterval = trailInterval;
        this.hitboxMultiplier = hitboxMultiplier;
        this.penetration = penetration;
    }

    public Bullet(ICombatEntity shooter, boolean penetration, int trailInterval) {
        this(shooter, penetration, trailInterval, 1);
    }

    public static void bulletHitEffect(Location location, Block hitBlock, boolean sound) {
        if (sound)
            SoundUtil.play("random.gun.ricochet", location, 0.8F, (float) (0.975 + Math.random() * 0.05));

        ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, hitBlock.getType(), hitBlock.getData(), location,
                3, 0, 0, 0, 0.1F);
        ParticleUtil.play(Particle.TOWN_AURA, location, 10, 0, 0, 0, 0);
    }

    public abstract void shoot(Location origin, Vector direction, float spread);

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

    public abstract void onHitEntity(Location location, ICombatEntity target, boolean isCrit);

    public void onHit(Location location) {    }

}
