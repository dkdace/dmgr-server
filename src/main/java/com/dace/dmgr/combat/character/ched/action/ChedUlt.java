package com.dace.dmgr.combat.character.ched.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.statuseffect.Burning;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.entity.temporary.Dummy;
import com.dace.dmgr.combat.entity.temporary.SummonEntity;
import com.dace.dmgr.combat.interaction.*;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public final class ChedUlt extends UltimateSkill {
    /** 처치 점수 제한시간 쿨타임 ID */
    public static final String KILL_SCORE_COOLDOWN_ID = "ChedUltKillScoreTimeLimit";
    /** 수정자 ID */
    private static final String MODIFIER_ID = "ChedUlt";
    /** 소환한 엔티티 */
    private ChedUltFireFloor summonEntity = null;

    public ChedUlt(@NonNull CombatUser combatUser) {
        super(combatUser, ChedUltInfo.getInstance());
    }

    @Override
    public int getCost() {
        return ChedUltInfo.COST;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && (combatUser.getSkill(ChedP1Info.getInstance()).isDurationFinished() ||
                !combatUser.getEntity().hasGravity());
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        setDuration();
        combatUser.setGlobalCooldown((int) ChedUltInfo.READY_DURATION);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, -ChedA3Info.READY_SLOW);
        combatUser.getWeapon().onCancelled();
        combatUser.getEntity().getInventory().setItem(30, new ItemStack(Material.AIR));

        SoundUtil.playNamedSound(NamedSound.COMBAT_CHED_ULT_USE, combatUser.getEntity().getLocation());

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            if (!combatUser.getSkill(ChedP1Info.getInstance()).isDurationFinished() && combatUser.getEntity().hasGravity())
                return false;

            Location loc = LocationUtil.getLocationFromOffset(combatUser.getArmLocation(true), 0, 0, 1.5);
            playUseTickEffect(loc, i);

            return true;
        }, isCancelled -> {
            onCancelled();
            if (isCancelled)
                return;

            Location location = combatUser.getArmLocation(true);
            new ChedUltProjectile().shoot(location);

            Location loc = LocationUtil.getLocationFromOffset(combatUser.getArmLocation(true), 0, 0, 1.5);
            TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
                playUseTickEffect(loc, i + ChedUltInfo.READY_DURATION);
                return true;
            }, 1, 20));
            SoundUtil.playNamedSound(NamedSound.COMBAT_CHED_ULT_USE_READY, location);
        }, 1, ChedUltInfo.READY_DURATION));
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
    }

    @Override
    public void reset() {
        super.reset();

        if (summonEntity != null)
            summonEntity.dispose();
    }

    /**
     * 사용 시 효과를 재생한다.
     *
     * @param location 사용 위치
     * @param i        인덱스
     */
    private void playUseTickEffect(@NonNull Location location, long i) {
        Vector vector = VectorUtil.getYawAxis(location);
        Vector axis = VectorUtil.getRollAxis(location);

        for (int j = 0; j < 2; j++) {
            long index = i * 2 + j;
            long angle = index * (i > 10 ? -3 : 3);
            double distance = index * 0.035;
            double forward = 0;
            if (i > 30) {
                forward = (index - 60) * 0.2;
                distance = 60 * 0.035 - (index - 60) * 0.01;
            }

            int angles = (i > 15 ? 4 : 6);
            for (int k = 0; k < angles * 2; k++) {
                angle += 360 / angles;
                Vector vec = VectorUtil.getRotatedVector(vector, axis, k < angles ? angle : -angle);
                Vector vec2 = vec.clone().multiply(distance + 0.6);
                Location loc2 = location.clone().add(vec2).add(location.getDirection().multiply(forward));

                ParticleUtil.play(Particle.DRIP_LAVA, loc2, 1, 0, 0, 0, 0);
                if (i > 30) {
                    ParticleUtil.play(Particle.FLAME, loc2, 0, vec.getX(), vec.getY(), vec.getZ(), 0.1);
                    ParticleUtil.play(Particle.FLAME, loc2, 0, vec.getX(), vec.getY(), vec.getZ(), 0.16);
                } else
                    ParticleUtil.play(Particle.FLAME, loc2, 0, vec.getX(), vec.getY(), vec.getZ(), 0.02 + index * 0.003);
            }
        }
    }

    /**
     * 화염 상태 효과 클래스.
     */
    private static final class ChedUltBurning extends Burning {
        private static final ChedUltBurning instance = new ChedUltBurning();

        private ChedUltBurning() {
            super(ChedUltInfo.FIRE_DAMAGE_PER_SECOND, false);
        }
    }

    private final class ChedUltProjectile extends Projectile {
        private ChedUltProjectile() {
            super(combatUser, ChedUltInfo.VELOCITY, ProjectileOption.builder().trailInterval(15).size(ChedUltInfo.SIZE)
                    .condition(combatUser::isEnemy).build());
        }

        @Override
        protected void onTrailInterval() {
            playTickEffect();
            SoundUtil.playNamedSound(NamedSound.COMBAT_CHED_ULT_TICK, getLocation());
        }

        private void playTickEffect() {
            Location loc = getLocation().clone();
            loc.setPitch(0);

            ParticleUtil.play(Particle.REDSTONE, loc, 20, 0.28, 0.28, 0.28, 0);
            ParticleUtil.play(Particle.REDSTONE, LocationUtil.getLocationFromOffset(loc, 0, -0.5, -0.6), 8,
                    0.2, 0.12, 0.2, 0);
            ParticleUtil.play(Particle.REDSTONE, LocationUtil.getLocationFromOffset(loc, 0, -0.7, -1.2), 8,
                    0.16, 0.08, 0.16, 0);
            ParticleUtil.play(Particle.REDSTONE, LocationUtil.getLocationFromOffset(loc, 0, -0.9, -1.8), 8,
                    0.12, 0.04, 0.12, 0);

            ParticleUtil.play(Particle.REDSTONE, LocationUtil.getLocationFromOffset(loc, 0, 0.4, 0.8), 8,
                    0.1, 0.16, 0.1, 0);
            ParticleUtil.play(Particle.REDSTONE, LocationUtil.getLocationFromOffset(loc, 0, 0.6, 1), 8,
                    0.1, 0.16, 0.1, 0);
            ParticleUtil.play(Particle.REDSTONE, LocationUtil.getLocationFromOffset(loc, 0, 0.8, 1.4), 8,
                    0.18, 0.16, 0.18, 0);
            ParticleUtil.play(Particle.REDSTONE, LocationUtil.getLocationFromOffset(loc, 0, 0.8, 1.6), 8,
                    0.24, 0.16, 0.24, 0);

            ParticleUtil.play(Particle.LAVA, LocationUtil.getLocationFromOffset(loc, -2.8, 1.7, 0), 3,
                    0, 0, 0, 0);
            ParticleUtil.play(Particle.LAVA, LocationUtil.getLocationFromOffset(loc, 2.8, 1.7, 0), 3,
                    0, 0, 0, 0);

            for (int i = 0; i < 6; i++) {
                Location loc1 = LocationUtil.getLocationFromOffset(loc, 0.7 + i * 0.4, 0.3 + i * (i < 3 ? 0.2 : 0.25), 0);
                Location loc2 = LocationUtil.getLocationFromOffset(loc, -0.7 - i * 0.4, 0.3 + i * (i < 3 ? 0.2 : 0.25), 0);
                Vector vec = VectorUtil.getSpreadedVector(getVelocity().clone().normalize(), 20);
                ParticleUtil.play(Particle.REDSTONE, loc1, 8, 0.1, 0.1 + i * 0.04, 0.1, 0);
                ParticleUtil.play(Particle.REDSTONE, loc2, 8, 0.1, 0.1 + i * 0.04, 0.1, 0);
                ParticleUtil.play(Particle.FLAME, loc1, 0, vec.getX(), vec.getY(), vec.getZ(), -0.25);
                ParticleUtil.play(Particle.FLAME, loc2, 0, vec.getX(), vec.getY(), vec.getZ(), -0.25);
            }
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            return true;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            if (!(target instanceof CombatUser || target instanceof Dummy))
                return true;

            Location loc = target.getHitboxLocation().add(0, target.getEntity().getHeight() / 2, 0).add(0, 0.1, 0);
            new ChedUltArea().emit(loc);

            ArmorStand armorStand = CombatUtil.spawnEntity(ArmorStand.class, loc);
            summonEntity = new ChedUltFireFloor(armorStand, combatUser);
            summonEntity.activate();

            for (Location loc2 : LocationUtil.getLine(getLocation(), loc, 0.4))
                ParticleUtil.play(Particle.FLAME, loc2, 5, 0.05, 0.05, 0.05, 0);

            SoundUtil.playNamedSound(NamedSound.COMBAT_CHED_ULT_EXPLODE, loc);
            ParticleUtil.play(Particle.EXPLOSION_HUGE, loc, 1, 0, 0, 0, 0);
            ParticleUtil.play(Particle.SMOKE_LARGE, loc, 400, 0.5, 0.5, 0.5, 0.2);
            ParticleUtil.play(Particle.SMOKE_NORMAL, loc, 600, 0.4, 0.4, 0.4, 0.4);
            ParticleUtil.play(Particle.LAVA, loc, 150, 3, 3, 3, 0);
            ParticleUtil.play(Particle.FLAME, loc, 400, 0.2, 0.2, 0.2, 0.25);

            return false;
        }

        private final class ChedUltArea extends Area {
            private ChedUltArea() {
                super(combatUser, ChedUltInfo.SIZE, ChedUltProjectile.this.condition.or(combatEntity -> combatEntity == ChedUlt.this.combatUser));
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            public boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                double distance = center.distance(location);
                int damage = CombatUtil.getDistantDamage(ChedUltInfo.DAMAGE, distance, ChedUltInfo.SIZE / 2.0, true);
                if (target.getDamageModule().damage(ChedUltProjectile.this, damage, DamageType.NORMAL, null, false, false)) {
                    target.getKnockbackModule().knockback(LocationUtil.getDirection(center, location.add(0, 0.5, 0)).multiply(ChedUltInfo.KNOCKBACK));
                    if (target instanceof CombatUser)
                        CooldownUtil.setCooldown(combatUser, KILL_SCORE_COOLDOWN_ID + target, ChedUltInfo.KILL_SCORE_TIME_LIMIT);
                }

                return !(target instanceof Barrier);
            }
        }
    }

    /**
     * 화염 지대 클래스.
     */
    public final class ChedUltFireFloor extends SummonEntity<ArmorStand> {
        private ChedUltFireFloor(@NonNull ArmorStand entity, @NonNull CombatUser owner) {
            super(
                    entity,
                    owner.getName() + "의 화염 지대",
                    owner,
                    false, true,
                    new FixedPitchHitbox(entity.getLocation(), 1, 1, 1, 0, 0.5, 0)
            );

            onInit();
        }

        private void onInit() {
            entity.setAI(false);
            entity.setSilent(true);
            entity.setInvulnerable(true);
            entity.setGravity(true);
            entity.setMarker(true);
            entity.setVisible(false);
        }

        @Override
        protected void onTick(long i) {
            Location loc = entity.getLocation().add(0, 0.1, 0);
            new ChedUltFireFloorArea().emit(loc);

            if (i % 4 == 0)
                SoundUtil.playNamedSound(NamedSound.COMBAT_CHED_ULT_FIRE_FLOOR_TICK, loc);
            playTickEffect();

            if (i >= ChedUltInfo.FIRE_FLOOR_DURATION)
                dispose();
        }

        /**
         * 표시 효과를 재생한다.
         */
        private void playTickEffect() {
            Location loc = entity.getLocation().add(0, 0.1, 0);
            ParticleUtil.play(Particle.FLAME, loc, 20, 4, 0, 4, 0);
            ParticleUtil.play(Particle.SMOKE_LARGE, loc, 6, 4, 0, 4, 0);
        }

        @Override
        public void dispose() {
            super.dispose();

            summonEntity = null;
        }

        private final class ChedUltFireFloorArea extends Area {
            private ChedUltFireFloorArea() {
                super(combatUser, ChedUltInfo.SIZE, combatUser::isEnemy);
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            public boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                if (target.getDamageModule().damage(combatUser, 0, DamageType.NORMAL, null,
                        false, false)) {
                    target.getStatusEffectModule().applyStatusEffect(combatUser, ChedUltBurning.instance, 10);
                    if (target instanceof CombatUser)
                        CooldownUtil.setCooldown(combatUser, KILL_SCORE_COOLDOWN_ID + target, ChedUltInfo.KILL_SCORE_TIME_LIMIT);
                }

                return !(target instanceof Barrier);
            }
        }
    }
}
