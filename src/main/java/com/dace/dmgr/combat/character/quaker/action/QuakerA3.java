package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.Barrier;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.*;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.function.Predicate;

@Getter
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
        combatUser.getWeapon().displayDurability(QuakerWeaponInfo.RESOURCE.USE);

        Location location = combatUser.getEntity().getEyeLocation();
        playUseSound(location);

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
            onReady();
        }, 1, QuakerA3Info.READY_DURATION));
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        setDuration(0);
        combatUser.getMoveModule().getSpeedStatus().removeModifier("QuakerA3");
        combatUser.getWeapon().displayDurability(QuakerWeaponInfo.RESOURCE.DEFAULT);
    }

    /**
     * 사용 시 효과음을 재생한다.
     *
     * @param location 사용 위치
     */
    private void playUseSound(Location location) {
        SoundUtil.play(Sound.BLOCK_LAVA_EXTINGUISH, location, 1, 0.8);
        SoundUtil.play("random.gun2.shovel_leftclick", location, 1, 0.5);
        SoundUtil.play("random.gun2.shovel_leftclick", location, 1, 0.8);
    }

    /**
     * 시전 완료 시 실행할 작업.
     */
    private void onReady() {
        Location loc = combatUser.getEntity().getLocation();
        new QuakerA3Projectile().shoot();
        playReadySound(loc);
    }

    /**
     * 시전 완료 시 효과음을 재생한다.
     *
     * @param location 사용 위치
     */
    private void playReadySound(Location location) {
        SoundUtil.play(Sound.ENTITY_GHAST_SHOOT, location, 2, 0.5);
        SoundUtil.play("new.item.trident.throw", location, 2, 0.7);
        SoundUtil.play(Sound.ENTITY_PLAYER_ATTACK_SWEEP, location, 2, 0.7);
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
            SoundUtil.play(Sound.ENTITY_GHAST_SHOOT, location, 0.6, 0.5);

            CombatEntity[] areaTargets = CombatUtil.getNearCombatEntities(combatUser.getGame(), location, size, condition);

            new QuakerA3Area(condition, areaTargets, direction).emit(location);
        }

        @Override
        protected boolean onHitBlock(@NonNull Location location, @NonNull Vector velocity, @NonNull Block hitBlock) {
            ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, hitBlock.getType(), hitBlock.getData(), location,
                    80, 0.2, 0.2, 0.2, 0.2);
            ParticleUtil.play(Particle.EXPLOSION_NORMAL, location, 50, 0.2, 0.2, 0.2, 0.4);
            onImpact(location.add(0, 0.1, 0));

            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location location, @NonNull Vector velocity, @NonNull Damageable target, boolean isCrit) {
            return !(target instanceof Barrier);
        }

        private void onImpact(@NonNull Location location) {
            SoundUtil.play(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, location, 2, 0.6);
            SoundUtil.play(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, location, 2, 0.7);
            SoundUtil.play(Sound.ENTITY_PLAYER_ATTACK_CRIT, location, 2, 0.7);

            for (Damageable target2 : targets) {
                if (target2.getNearestLocationOfHitboxes(location).distance(location) < QuakerA3Info.SIZE)
                    target2.getDamageModule().damage(combatUser, QuakerA3Info.DAMAGE, DamageType.NORMAL, false, true);
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

                return !(target instanceof Barrier);
            }
        }
    }
}
