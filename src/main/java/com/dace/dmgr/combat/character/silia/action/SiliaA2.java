package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Living;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.combat.interaction.ProjectileOption;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public final class SiliaA2 extends ActiveSkill {
    SiliaA2(@NonNull CombatUser combatUser) {
        super(combatUser, SiliaA2Info.getInstance(), 1);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_2, ActionKey.RIGHT_CLICK};
    }

    @Override
    public long getDefaultCooldown() {
        return SiliaA2Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && isDurationFinished() && combatUser.getSkill(SiliaP2Info.getInstance()).isDurationFinished() &&
                combatUser.getSkill(SiliaUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.setGlobalCooldown(SiliaA2Info.GLOBAL_COOLDOWN);
        if (!combatUser.getSkill(SiliaA3Info.getInstance()).isDurationFinished())
            combatUser.getSkill(SiliaA3Info.getInstance()).onCancelled();

        SoundUtil.playNamedSound(NamedSound.COMBAT_SILIA_A2_USE, combatUser.getEntity().getLocation());

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            Location location = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation(), 0, 0, 1);
            Vector vector = VectorUtil.getYawAxis(location).multiply(0.8);
            Vector axis = VectorUtil.getRollAxis(location);

            for (int j = 0; j < 6; j++) {
                Vector vec = VectorUtil.getRotatedVector(vector, axis, i * 23 + j * 60).multiply(1.6 - i * 0.2);
                Location loc = location.clone().add(vec);
                ParticleUtil.play(Particle.EXPLOSION_NORMAL, loc, 0, vec.getX(), vec.getY(), vec.getZ(), 0.2);
            }

            return true;
        }, isCancelled -> {
            onCancelled();

            new SiliaA2Projectile().shoot();

            SoundUtil.playNamedSound(NamedSound.COMBAT_SILIA_A2_USE_READY, combatUser.getEntity().getLocation());
        }, 1, SiliaA2Info.READY_DURATION));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        setDuration(0);
    }

    private final class SiliaA2Projectile extends Projectile {
        int i = 0;

        private SiliaA2Projectile() {
            super(combatUser, SiliaA2Info.VELOCITY, ProjectileOption.builder().trailInterval(4).size(SiliaA2Info.SIZE)
                    .maxDistance(SiliaA2Info.DISTANCE).condition(combatUser::isEnemy).build());
        }

        @Override
        protected void trail() {
            i++;

            Vector vector = VectorUtil.getYawAxis(location).multiply(0.8);
            Vector axis = VectorUtil.getRollAxis(location);

            Vector vec1 = VectorUtil.getSpreadedVector(VectorUtil.getRotatedVector(vector, axis, i * 12), 8);
            Vector vec2 = VectorUtil.getSpreadedVector(VectorUtil.getRotatedVector(vector, axis, 180 + i * 12), 8);
            ParticleUtil.play(Particle.EXPLOSION_NORMAL, location.clone().add(vec1), 0, vec1.getX(), vec1.getY(), vec1.getZ(), 0.25);
            ParticleUtil.play(Particle.EXPLOSION_NORMAL, location.clone().add(vec2), 0, vec2.getX(), vec2.getY(), vec2.getZ(), 0.25);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, location.clone().add(vec1), 3,
                    0.3, 0.3, 0.3, 255, 255, 255);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, location.clone().add(vec2), 3,
                    0.3, 0.3, 0.3, 255, 255, 255);
        }

        @Override
        protected void onHit() {
            for (int j = 0; j < 40; j++) {
                Vector vec = VectorUtil.getSpreadedVector(new Vector(0, 1, 0), 60);
                ParticleUtil.play(Particle.EXPLOSION_NORMAL, location, 0, vec.getX(), vec.getY(), vec.getZ(),
                        0.3 + DMGR.getRandom().nextDouble() * 0.4);
            }
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            CombatUtil.playBlockHitEffect(location, hitBlock, 3);
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            setCooldown(getDefaultCooldown() / 2);

            if (target.getDamageModule().damage(this, SiliaA2Info.DAMAGE, DamageType.NORMAL, location,
                    SiliaT1.isBackAttack(velocity, target) ? SiliaT1Info.CRIT_MULTIPLIER : 1, true)) {
                target.getKnockbackModule().knockback(new Vector(0, 0.8, 0), true);

                Location loc = target.getEntity().getLocation();
                loc.setPitch(0);
                loc = LocationUtil.getLocationFromOffset(loc, 0, 0, -1.5);
                for (Location trailLoc : LocationUtil.getLine(combatUser.getEntity().getLocation(), loc, 0.5))
                    ParticleUtil.play(Particle.END_ROD, trailLoc.add(0, 1, 0), 3, 0, 0, 0, 0.05);
                SoundUtil.playNamedSound(NamedSound.COMBAT_SILIA_A2_HIT_ENTITY, location);

                if (target instanceof Living && LocationUtil.canPass(combatUser.getEntity().getEyeLocation(), loc.clone().add(0, target.getEntity().getHeight() / 2, 0)) &&
                        (!(target instanceof CombatUser) || !((CombatUser) target).isDead())) {
                    combatUser.teleport(loc);
                    combatUser.push(new Vector(0, 0.8, 0), true);
                    if (target instanceof CombatUser)
                        combatUser.addScore("적 띄움", SiliaA2Info.DAMAGE_SCORE);
                }
            }

            return false;
        }
    }
}
