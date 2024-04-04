package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.Barrier;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.interaction.*;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.function.Predicate;

public final class QuakerA3 extends ActiveSkill {
    public QuakerA3(@NonNull CombatUser combatUser) {
        super(3, combatUser, QuakerA3Info.getInstance(), 2);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_3};
    }

    @Override
    public long getDefaultCooldown() {
        return QuakerA3Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && isDurationFinished() && combatUser.getSkill(QuakerA1Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        combatUser.getWeapon().onCancelled();
        combatUser.setGlobalCooldown(16);
        setDuration();
        combatUser.getMoveModule().getSpeedStatus().addModifier("QuakerA3", -100);
        combatUser.getWeapon().setVisible(false);
        combatUser.playMeleeAttackAnimation(-7, 12, true);
        SoundUtil.playNamedSound(NamedSound.COMBAT_QUAKER_A3_USE, combatUser.getEntity().getLocation());

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            for (int j = 0; j < i; j++) {
                Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation(), 0, 0, 1);
                Vector vector = VectorUtil.getYawAxis(loc).multiply(-1);
                Vector axis = VectorUtil.getPitchAxis(loc);

                Vector vec = VectorUtil.getRotatedVector(vector, axis, 90 + 30 * (j - 2));
                new QuakerA3Effect().shoot(loc.add(vec), vec);
            }

            return true;
        }, isCancelled -> {
            onCancelled();

            new QuakerA3Projectile().shoot();
            SoundUtil.playNamedSound(NamedSound.COMBAT_QUAKER_A3_USE_READY, combatUser.getEntity().getLocation());
        }, 1, QuakerA3Info.READY_DURATION));
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        setDuration(0);
        combatUser.getMoveModule().getSpeedStatus().removeModifier("QuakerA3");
        combatUser.getWeapon().setVisible(true);
    }

    private class QuakerA3Effect extends Hitscan {
        public QuakerA3Effect() {
            super(combatUser, HitscanOption.builder().trailInterval(100).maxDistance(0.3).condition(combatUser::isEnemy).build());
        }

        @Override
        protected void trail(@NonNull Location location, @NonNull Vector direction) {
            Location trailLoc1 = LocationUtil.getLocationFromOffset(location, -0.25, 0, 0);
            Location trailLoc2 = LocationUtil.getLocationFromOffset(location, 0, 0, 0);
            Location trailLoc3 = LocationUtil.getLocationFromOffset(location, 0.25, 0, 0);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, trailLoc1, 2, 0.12, 0.12, 0.12,
                    200, 200, 200);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, trailLoc2, 2, 0.12, 0.12, 0.12,
                    200, 200, 200);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, trailLoc3, 2, 0.12, 0.12, 0.12,
                    200, 200, 200);
        }

        @Override
        protected boolean onHitBlock(@NonNull Location location, @NonNull Vector velocity, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location location, @NonNull Vector velocity, @NonNull Damageable target, boolean isCrit) {
            return true;
        }

        @Override
        protected void onDestroy(@NonNull Location location) {
            Location trailLoc1 = LocationUtil.getLocationFromOffset(location, -0.25, 0, 0);
            Location trailLoc2 = LocationUtil.getLocationFromOffset(location, 0, 0, 0);
            Location trailLoc3 = LocationUtil.getLocationFromOffset(location, 0.25, 0, 0);
            ParticleUtil.play(Particle.CRIT, trailLoc1, 3, 0.07, 0.07, 0.07, 0);
            ParticleUtil.play(Particle.CRIT, trailLoc2, 3, 0.07, 0.07, 0.07, 0);
            ParticleUtil.play(Particle.CRIT, trailLoc3, 3, 0.07, 0.07, 0.07, 0);
        }
    }

    private class QuakerA3Projectile extends Projectile {
        private final HashSet<Damageable> targets = new HashSet<>();

        private QuakerA3Projectile() {
            super(QuakerA3.this.combatUser, QuakerA3Info.VELOCITY, ProjectileOption.builder().trailInterval(15).size(QuakerA3Info.SIZE)
                    .maxDistance(QuakerA3Info.DISTANCE).condition(QuakerA3.this.combatUser::isEnemy).build());
        }

        @Override
        protected void trail(@NonNull Location location, @NonNull Vector direction) {
            for (int i = 0; i < 8; i++) {
                Vector vector = VectorUtil.getYawAxis(location).multiply(-1);
                Vector axis = VectorUtil.getPitchAxis(location);

                Vector vec = VectorUtil.getRotatedVector(vector, axis, 90 + 30 * (i - 2)).multiply(0.6);
                Location loc = location.clone().add(vec);
                new QuakerA3Effect().shoot(loc, vec);

                Vector vec2 = VectorUtil.getSpreadedVector(direction, 30);
                ParticleUtil.play(Particle.EXPLOSION_NORMAL, location, 0, vec2.getX(), vec2.getY(), vec2.getZ(), 1.2);
            }
            SoundUtil.playNamedSound(NamedSound.COMBAT_QUAKER_A3_TICK, location);

            CombatEntity[] areaTargets = CombatUtil.getNearCombatEntities(combatUser.getGame(), location, size, condition);

            new QuakerA3Area(condition, areaTargets, direction).emit(location);
        }

        @Override
        protected boolean onHitBlock(@NonNull Location location, @NonNull Vector velocity, @NonNull Block hitBlock) {
            ParticleUtil.playBlockHitEffect(location, hitBlock, 5);
            ParticleUtil.play(Particle.EXPLOSION_NORMAL, location, 50, 0.2, 0.2, 0.2, 0.4);
            onImpact(location.add(0, 0.1, 0));

            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location location, @NonNull Vector velocity, @NonNull Damageable target, boolean isCrit) {
            return !(target instanceof Barrier);
        }

        private void onImpact(@NonNull Location location) {
            SoundUtil.playNamedSound(NamedSound.COMBAT_QUAKER_A3_HIT, location);

            for (Damageable target2 : targets) {
                if (target2.getNearestLocationOfHitboxes(location).distance(location) < QuakerA3Info.SIZE)
                    target2.getDamageModule().damage(combatUser, QuakerA3Info.DAMAGE, DamageType.NORMAL, location, false, true);
            }
        }

        private class QuakerA3Area extends Area {
            private final Vector direction;

            private QuakerA3Area(Predicate<CombatEntity> condition, CombatEntity[] targets, Vector direction) {
                super(QuakerA3.this.combatUser, QuakerA3Info.SIZE, condition, targets);
                this.direction = direction;
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            public boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                if (QuakerA3Projectile.this.targets.add(target) && targets.length > 1) {
                    ParticleUtil.play(Particle.CRIT, location, 50, 0, 0, 0, 0.4);
                    onImpact(location);
                }
                target.getKnockbackModule().knockback(direction.clone().multiply(2), true);
                target.getStatusEffectModule().applyStatusEffect(StatusEffectType.SNARE, QuakerA3Info.SNARE_DURATION);

                return !(target instanceof Barrier);
            }
        }
    }
}
