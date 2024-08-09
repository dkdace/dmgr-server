package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.statuseffect.Snare;
import com.dace.dmgr.combat.entity.temporary.Barrier;
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
    /** 수정자 ID */
    private static final String MODIFIER_ID = "QuakerA3";

    QuakerA3(@NonNull CombatUser combatUser) {
        super(combatUser, QuakerA3Info.getInstance(), 2);
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
        setDuration();
        combatUser.getWeapon().onCancelled();
        combatUser.getWeapon().setVisible(false);
        combatUser.setGlobalCooldown(QuakerA3Info.GLOBAL_COOLDOWN);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, -100);
        combatUser.playMeleeAttackAnimation(-7, 12, true);

        SoundUtil.playNamedSound(NamedSound.COMBAT_QUAKER_A3_USE, combatUser.getEntity().getLocation());

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation(), 0, 0, 1);
            Vector vector = VectorUtil.getYawAxis(loc).multiply(-1);
            Vector axis = VectorUtil.getPitchAxis(loc);

            for (int j = 0; j < i; j++) {
                Vector vec = VectorUtil.getRotatedVector(vector, axis, 90 + 30 * (j - 2));
                new QuakerA3Effect().shoot(loc.clone().add(vec), vec);
            }

            return true;
        }, isCancelled -> {
            onCancelled();

            new QuakerA3Projectile().shoot();

            SoundUtil.playNamedSound(NamedSound.COMBAT_QUAKER_A3_USE_READY, combatUser.getEntity().getLocation());
        }, 1, QuakerA3Info.READY_DURATION));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        setDuration(0);
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
        combatUser.getWeapon().setVisible(true);
    }

    private final class QuakerA3Effect extends Hitscan {
        private QuakerA3Effect() {
            super(combatUser, HitscanOption.builder().trailInterval(100).maxDistance(0.4).condition(combatUser::isEnemy).build());
        }

        @Override
        protected void onTrailInterval() {
            for (int i = 0; i < 3; i++) {
                Location loc = LocationUtil.getLocationFromOffset(getLocation(), -0.25 + i * 0.25, 0, 0);
                ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc, 2, 0.12, 0.12, 0.12,
                        200, 200, 200);
            }
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            return true;
        }

        @Override
        protected void onDestroy() {
            for (int i = 0; i < 3; i++) {
                Location loc = LocationUtil.getLocationFromOffset(getLocation(), -0.25 + i * 0.25, 0, 0);
                ParticleUtil.play(Particle.CRIT, loc, 3, 0.07, 0.07, 0.07, 0);
            }
        }
    }

    private final class QuakerA3Projectile extends Projectile {
        private final HashSet<Damageable> targets = new HashSet<>();

        private QuakerA3Projectile() {
            super(combatUser, QuakerA3Info.VELOCITY, ProjectileOption.builder().trailInterval(15).size(QuakerA3Info.SIZE)
                    .maxDistance(QuakerA3Info.DISTANCE).condition(combatUser::isEnemy).build());
        }

        @Override
        protected void onTrailInterval() {
            Vector vector = VectorUtil.getYawAxis(getLocation()).multiply(-1);
            Vector axis = VectorUtil.getPitchAxis(getLocation());

            for (int i = 0; i < 8; i++) {
                Vector vec = VectorUtil.getRotatedVector(vector, axis, 90 + 30 * (i - 2)).multiply(0.6);
                Location loc = getLocation().clone().add(vec);
                new QuakerA3Effect().shoot(loc, vec);

                Vector vec2 = VectorUtil.getSpreadedVector(getVelocity().clone().normalize(), 30);
                ParticleUtil.play(Particle.EXPLOSION_NORMAL, getLocation(), 0, vec2.getX(), vec2.getY(), vec2.getZ(), 1.2);
            }
            SoundUtil.playNamedSound(NamedSound.COMBAT_QUAKER_A3_TICK, getLocation());

            CombatEntity[] areaTargets = CombatUtil.getNearCombatEntities(combatUser.getGame(), getLocation(), size, condition);
            new QuakerA3Area(condition, areaTargets.length).emit(getLocation());
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            onImpact(getLocation());

            CombatEffectUtil.playBlockHitEffect(getLocation(), hitBlock, 5);
            ParticleUtil.play(Particle.EXPLOSION_NORMAL, getLocation(), 50, 0.2, 0.2, 0.2, 0.4);

            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            return !(target instanceof Barrier);
        }

        private void onImpact(@NonNull Location location) {
            for (Damageable target2 : targets) {
                if (target2.getNearestLocationOfHitboxes(location).distance(location) < QuakerA3Info.SIZE &&
                        target2.getDamageModule().damage(this, QuakerA3Info.DAMAGE, DamageType.NORMAL, location, false, true) &&
                        target2 instanceof CombatUser)
                    combatUser.addScore("돌풍 강타", QuakerA3Info.DAMAGE_SCORE);
            }

            SoundUtil.playNamedSound(NamedSound.COMBAT_QUAKER_A3_HIT, location);
        }

        private final class QuakerA3Area extends Area {
            private final int targetCount;

            private QuakerA3Area(Predicate<CombatEntity> condition, int targetCount) {
                super(combatUser, QuakerA3Info.SIZE, condition);
                this.targetCount = targetCount;
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            public boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                if (targets.add(target) && targetCount > 1) {
                    onImpact(location);
                    ParticleUtil.play(Particle.CRIT, location, 50, 0, 0, 0, 0.4);
                }
                target.getKnockbackModule().knockback(getVelocity().clone().normalize().multiply(QuakerA3Info.KNOCKBACK), true);
                target.getStatusEffectModule().applyStatusEffect(combatUser, Snare.getInstance(), QuakerA3Info.SNARE_DURATION);

                return !(target instanceof Barrier);
            }
        }
    }
}
