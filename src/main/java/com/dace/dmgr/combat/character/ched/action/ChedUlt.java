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
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.Timespan;
import com.dace.dmgr.util.Timestamp;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.Vector;

import java.util.WeakHashMap;
import java.util.function.LongConsumer;

public final class ChedUlt extends UltimateSkill {
    /** 수정자 ID */
    private static final String MODIFIER_ID = "ChedUlt";
    /** 처치 점수 제한시간 타임스탬프 목록 (피격자 : 종료 시점) */
    private final WeakHashMap<CombatUser, Timestamp> killScoreTimeLimitTimestampMap = new WeakHashMap<>();
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
        ChedP1 skillp1 = combatUser.getSkill(ChedP1Info.getInstance());
        return super.canUse(actionKey) && (skillp1.isDurationFinished() || skillp1.isHanging());
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        setDuration();
        combatUser.setGlobalCooldown((int) ChedUltInfo.READY_DURATION);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, -ChedUltInfo.READY_SLOW);
        combatUser.getWeapon().onCancelled();
        ((ChedWeapon) combatUser.getWeapon()).setCanShoot(false);

        ChedUltInfo.SOUND.USE.play(combatUser.getEntity().getLocation());

        ChedP1 skillp1 = combatUser.getSkill(ChedP1Info.getInstance());

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            if (!skillp1.isDurationFinished() && !skillp1.isHanging())
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

            ChedUltInfo.SOUND.USE_READY.play(location);
            Location loc = LocationUtil.getLocationFromOffset(location, 0, 0, 1.5);

            TaskUtil.addTask(taskRunner, new IntervalTask((LongConsumer) i -> playUseTickEffect(loc, i + ChedUltInfo.READY_DURATION),
                    1, 20));
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

                if (i <= 30)
                    ChedUltInfo.PARTICLE.USE_TICK_1.play(loc2, vec, index / 60.0);
                else
                    ChedUltInfo.PARTICLE.USE_TICK_2.play(loc2, vec);
            }
        }
    }

    /**
     * 플레이어에게 보너스 점수를 지급한다.
     *
     * @param victim 피격자
     * @param score  점수 (처치 기여도)
     */
    public void applyBonusScore(@NonNull CombatUser victim, int score) {
        Timestamp expiration = killScoreTimeLimitTimestampMap.get(victim);
        if (expiration != null && expiration.isAfter(Timestamp.now()))
            combatUser.addScore("궁극기 보너스", ChedUltInfo.KILL_SCORE * score / 100.0);
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
            ChedUltInfo.SOUND.TICK.play(getLocation());
        }

        private void playTickEffect() {
            Location loc = getLocation().clone();
            loc.setPitch(0);

            ChedUltInfo.PARTICLE.BULLET_TRAIL_CORE.play(loc);

            ChedUltInfo.PARTICLE.BULLET_TRAIL_SHAPE.play(LocationUtil.getLocationFromOffset(loc, 0, -0.5, -0.6),
                    0.2, 0.12);
            ChedUltInfo.PARTICLE.BULLET_TRAIL_SHAPE.play(LocationUtil.getLocationFromOffset(loc, 0, -0.7, -1.2),
                    0.16, 0.08);
            ChedUltInfo.PARTICLE.BULLET_TRAIL_SHAPE.play(LocationUtil.getLocationFromOffset(loc, 0, -0.9, -1.8),
                    0.12, 0.04);

            ChedUltInfo.PARTICLE.BULLET_TRAIL_SHAPE.play(LocationUtil.getLocationFromOffset(loc, 0, 0.4, 0.8),
                    0.1, 0.16);
            ChedUltInfo.PARTICLE.BULLET_TRAIL_SHAPE.play(LocationUtil.getLocationFromOffset(loc, 0, 0.6, 1),
                    0.1, 0.16);
            ChedUltInfo.PARTICLE.BULLET_TRAIL_SHAPE.play(LocationUtil.getLocationFromOffset(loc, 0, 0.8, 1.4),
                    0.18, 0.16);
            ChedUltInfo.PARTICLE.BULLET_TRAIL_SHAPE.play(LocationUtil.getLocationFromOffset(loc, 0, 0.8, 1.6),
                    0.24, 0.16);

            ChedUltInfo.PARTICLE.BULLET_TRAIL_DECO_1.play(LocationUtil.getLocationFromOffset(loc, -2.8, 1.7, 0));
            ChedUltInfo.PARTICLE.BULLET_TRAIL_DECO_1.play(LocationUtil.getLocationFromOffset(loc, 2.8, 1.7, 0));

            for (int i = 0; i < 6; i++) {
                Location loc1 = LocationUtil.getLocationFromOffset(loc, 0.7 + i * 0.4, 0.3 + i * (i < 3 ? 0.2 : 0.25), 0);
                Location loc2 = LocationUtil.getLocationFromOffset(loc, -0.7 - i * 0.4, 0.3 + i * (i < 3 ? 0.2 : 0.25), 0);
                Vector vec = VectorUtil.getSpreadedVector(getVelocity().clone().normalize(), 20);

                ChedUltInfo.PARTICLE.BULLET_TRAIL_SHAPE.play(loc1, 0.1, 0.1 + i * 0.04);
                ChedUltInfo.PARTICLE.BULLET_TRAIL_SHAPE.play(loc2, 0.1, 0.1 + i * 0.04);
                ChedUltInfo.PARTICLE.BULLET_TRAIL_DECO_2.play(loc1, vec);
                ChedUltInfo.PARTICLE.BULLET_TRAIL_DECO_2.play(loc2, vec);
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

            summonEntity = new ChedUltFireFloor(CombatUtil.spawnEntity(ArmorStand.class, loc), combatUser);
            summonEntity.activate();

            for (Location loc2 : LocationUtil.getLine(getLocation(), loc, 0.4))
                ChedUltInfo.PARTICLE.HIT_ENTITY.play(loc2);

            ChedUltInfo.SOUND.EXPLODE.play(loc);
            ChedUltInfo.PARTICLE.EXPLODE.play(loc);

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
                if (target.getDamageModule().damage(ChedUltProjectile.this, 0, DamageType.NORMAL, null,
                        false, false) && target instanceof CombatUser)
                    killScoreTimeLimitTimestampMap.put((CombatUser) target, Timestamp.now().plus(Timespan.ofTicks(ChedUltInfo.KILL_SCORE_TIME_LIMIT)));

                double distance = center.distance(location);
                double damage = CombatUtil.getDistantDamage(ChedUltInfo.DAMAGE, distance, ChedUltInfo.SIZE / 2.0);
                if (target.getDamageModule().damage(ChedUltProjectile.this, damage, DamageType.NORMAL, null,
                        false, false))
                    target.getKnockbackModule().knockback(LocationUtil.getDirection(getLocation(), location.add(0, 1, 0))
                            .multiply(ChedUltInfo.KNOCKBACK));

                return !(target instanceof Barrier);
            }
        }
    }

    /**
     * 화염 지대 클래스.
     */
    private final class ChedUltFireFloor extends SummonEntity<ArmorStand> {
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
                ChedUltInfo.SOUND.FIRE_FLOOR_TICK.play(loc);
            ChedUltInfo.PARTICLE.FIRE_FLOOR_TICK.play(loc);

            if (i >= ChedUltInfo.FIRE_FLOOR_DURATION)
                dispose();
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
                        killScoreTimeLimitTimestampMap.put((CombatUser) target, Timestamp.now().plus(Timespan.ofTicks(ChedUltInfo.KILL_SCORE_TIME_LIMIT)));
                }

                return !(target instanceof Barrier);
            }
        }
    }
}
