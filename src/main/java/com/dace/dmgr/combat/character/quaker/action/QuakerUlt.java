package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.statuseffect.Slow;
import com.dace.dmgr.combat.entity.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.combat.interaction.HitscanOption;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.combat.interaction.ProjectileOption;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public final class QuakerUlt extends UltimateSkill {
    public QuakerUlt(@NonNull CombatUser combatUser) {
        super(4, combatUser, QuakerUltInfo.getInstance());
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public int getCost() {
        return QuakerUltInfo.COST;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && isDurationFinished() && !combatUser.getSkill(QuakerA1Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        combatUser.getWeapon().onCancelled();
        combatUser.setGlobalCooldown(16);
        setDuration();
        combatUser.getMoveModule().getSpeedStatus().addModifier("QuakerUlt", -100);
        combatUser.getWeapon().setVisible(false);
        combatUser.playMeleeAttackAnimation(-10, 16, false);

        TaskUtil.addTask(taskRunner, new DelayTask(() -> {
            int delay = 0;
            for (int i = 0; i < 8; i++) {
                final int index = i;

                switch (i) {
                    case 1:
                        delay += 2;
                        break;
                    case 2:
                    case 4:
                    case 6:
                    case 7:
                        delay += 1;
                        break;
                }

                TaskUtil.addTask(taskRunner, new DelayTask(() -> {
                    Location loc = combatUser.getEntity().getEyeLocation();
                    Vector vector = VectorUtil.getPitchAxis(loc);
                    Vector axis = VectorUtil.getYawAxis(loc);

                    Vector vec = VectorUtil.getRotatedVector(vector, axis, (index + 1) * 20);
                    new QuakerUltEffect().shoot(loc, vec);
                    CombatUtil.addYawAndPitch(combatUser.getEntity(), 0.8, 0.1);

                    if (index % 2 == 0)
                        SoundUtil.play(NamedSound.COMBAT_QUAKER_WEAPON_USE, loc.add(vec));
                    if (index == 7) {
                        CombatUtil.addYawAndPitch(combatUser.getEntity(), -1, -0.7);
                        onCancelled();
                        onReady();
                    }
                }, delay));
            }
        }, 2));
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        setDuration(0);
        combatUser.getMoveModule().getSpeedStatus().removeModifier("QuakerUlt");
        combatUser.getWeapon().setVisible(true);
    }

    /**
     * 시전 완료 시 실행할 작업.
     */
    private void onReady() {
        Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation(), 0, 0.3, 0);
        SoundUtil.play(NamedSound.COMBAT_QUAKER_ULT_USE_READY, loc);
        ParticleUtil.play(Particle.CRIT, LocationUtil.getLocationFromOffset(loc, 0, 0, 1.5), 100,
                0.2, 0.2, 0.2, 0.6);
        Set<CombatEntity> targets = new HashSet<>();

        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 4; j++) {
                Vector vector = VectorUtil.getPitchAxis(loc);
                Vector axis = VectorUtil.getYawAxis(loc);

                axis = VectorUtil.getRotatedVector(axis, vector, 13 * (j - 1.5));
                vector = VectorUtil.getRotatedVector(vector, vector, 13 * (j - 1.5));
                Vector vec = VectorUtil.getRotatedVector(vector, axis, 90 + 12 * (i - 3.5));
                new QuakerUltProjectile(targets).shoot(loc, vec);
            }
        }
        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            CombatUtil.addYawAndPitch(combatUser.getEntity(), (DMGR.getRandom().nextDouble() - DMGR.getRandom().nextDouble()) * 10,
                    (DMGR.getRandom().nextDouble() - DMGR.getRandom().nextDouble()) * 8);
            return true;
        }, 1, 6));
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static class QuakerUltSlow extends Slow {
        private static final QuakerUltSlow instance = new QuakerUltSlow();

        @Override
        @NonNull
        public String getName() {
            return super.getName() + "QuakerUlt";
        }

        @Override
        public void onStart(@NonNull CombatEntity combatEntity) {
            if (combatEntity instanceof Movable)
                ((Movable) combatEntity).getMoveModule().getSpeedStatus().addModifier("QuakerUlt", -QuakerUltInfo.SLOW);
        }

        @Override
        public void onEnd(@NonNull CombatEntity combatEntity) {
            if (combatEntity instanceof Movable)
                ((Movable) combatEntity).getMoveModule().getSpeedStatus().removeModifier("QuakerUlt");
        }
    }

    private class QuakerUltEffect extends Hitscan {
        public QuakerUltEffect() {
            super(combatUser, HitscanOption.builder().trailInterval(6).maxDistance(QuakerWeaponInfo.DISTANCE)
                    .condition(combatUser::isEnemy).build());
        }

        @Override
        protected void trail(@NonNull Location location, @NonNull Vector direction) {
            if (location.distance(combatUser.getEntity().getEyeLocation()) <= 1)
                return;

            Location trailLoc = LocationUtil.getLocationFromOffset(location, 0, -0.3, 0);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, trailLoc, 12, 0.3, 0.3, 0.3,
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
            Location trailLoc = LocationUtil.getLocationFromOffset(location, 0, -0.3, 0);
            ParticleUtil.play(Particle.CRIT, trailLoc, 30, 0.15, 0.15, 0.15, 0.05);
        }
    }

    private class QuakerUltProjectile extends Projectile {
        private final Set<CombatEntity> targets;

        private QuakerUltProjectile(Set<CombatEntity> targets) {
            super(QuakerUlt.this.combatUser, QuakerUltInfo.VELOCITY, ProjectileOption.builder().trailInterval(14).size(1)
                    .maxDistance(QuakerUltInfo.DISTANCE).condition(QuakerUlt.this.combatUser::isEnemy).build());

            this.targets = targets;
        }

        @Override
        protected void trail(@NonNull Location location, @NonNull Vector direction) {
            Vector vec = VectorUtil.getSpreadedVector(direction, 15);
            ParticleUtil.play(Particle.EXPLOSION_NORMAL, location, 0, vec.getX(), vec.getY(), vec.getZ(), 1);
            ParticleUtil.play(Particle.CRIT, location, 4, 0.2, 0.2, 0.2, 0.1);
        }

        @Override
        protected boolean onHitBlock(@NonNull Location location, @NonNull Vector velocity, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location location, @NonNull Vector velocity, @NonNull Damageable target, boolean isCrit) {
            if (targets.add(target)) {
                target.getDamageModule().damage(combatUser, QuakerUltInfo.DAMAGE, DamageType.NORMAL, location, false, false);
                target.getStatusEffectModule().applyStatusEffect(StatusEffectType.STUN, QuakerUltInfo.STUN_DURATION);
                target.getStatusEffectModule().applyStatusEffect(StatusEffectType.SLOW, QuakerUltSlow.instance, QuakerUltInfo.SLOW_DURATION);
                target.getKnockbackModule().knockback(LocationUtil.getDirection(combatUser.getEntity().getLocation(),
                        target.getEntity().getLocation().add(0, 1, 0)).multiply(3), true);

                ParticleUtil.play(Particle.CRIT, location, 60, 0, 0, 0, 0.4);
            }

            return !(target instanceof Barrier);
        }
    }
}
