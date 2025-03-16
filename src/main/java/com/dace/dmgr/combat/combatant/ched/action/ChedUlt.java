package com.dace.dmgr.combat.combatant.ched.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.HasBonusScore;
import com.dace.dmgr.combat.action.skill.Summonable;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.action.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.action.skill.module.EntityModule;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.entity.module.statuseffect.Burning;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.entity.temporary.Dummy;
import com.dace.dmgr.combat.entity.temporary.SummonEntity;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.MainHand;
import org.bukkit.util.Vector;

import java.util.function.LongConsumer;

@Getter
public final class ChedUlt extends UltimateSkill implements Summonable<ChedUlt.ChedUltFireFloor>, HasBonusScore {
    /** 수정자 ID */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(-ChedUltInfo.READY_SLOW);

    /** 소환 엔티티 모듈 */
    @NonNull
    private final EntityModule<ChedUltFireFloor> entityModule;
    /** 보너스 점수 모듈 */
    @NonNull
    private final BonusScoreModule bonusScoreModule;

    public ChedUlt(@NonNull CombatUser combatUser) {
        super(combatUser, ChedUltInfo.getInstance(), Timespan.MAX, ChedUltInfo.COST);

        entityModule = new EntityModule<>(this);
        bonusScoreModule = new BonusScoreModule(this, "궁극기 보너스", ChedUltInfo.KILL_SCORE);
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
        combatUser.setGlobalCooldown(ChedUltInfo.READY_DURATION);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);
        combatUser.getWeapon().cancel();
        ((ChedWeapon) combatUser.getWeapon()).setCanShoot(false);

        ChedUltInfo.SOUND.USE.play(combatUser.getLocation());

        ChedP1 skillp1 = combatUser.getSkill(ChedP1Info.getInstance());

        addActionTask(new IntervalTask(i -> {
            if (!skillp1.isDurationFinished() && !skillp1.isHanging())
                return false;

            Location loc = LocationUtil.getLocationFromOffset(combatUser.getArmLocation(MainHand.RIGHT), 0, 0, 1.5);
            playUseTickEffect(loc, i);

            return true;
        }, isCancelled -> {
            cancel();
            if (isCancelled)
                return;

            Location location = combatUser.getArmLocation(MainHand.RIGHT);
            new ChedUltProjectile().shot(location);

            ChedUltInfo.SOUND.USE_READY.play(location);
            Location loc = LocationUtil.getLocationFromOffset(location, 0, 0, 1.5);

            addActionTask(new IntervalTask((LongConsumer) i -> playUseTickEffect(loc, i + ChedUltInfo.READY_DURATION.toTicks()),
                    1, 20));
        }, 1, ChedUltInfo.READY_DURATION.toTicks()));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER);
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
     * 화염 상태 효과 클래스.
     */
    private static final class ChedUltBurning extends Burning {
        private static final ChedUltBurning instance = new ChedUltBurning();

        private ChedUltBurning() {
            super(ChedUltInfo.FIRE_DAMAGE_PER_SECOND, false);
        }
    }

    private final class ChedUltProjectile extends Projectile<Damageable> {
        private ChedUltProjectile() {
            super(combatUser, ChedUltInfo.VELOCITY,
                    CombatUtil.EntityCondition.enemy(combatUser).and(combatEntity ->
                            combatEntity instanceof CombatUser || combatEntity instanceof Dummy),
                    Option.builder().size(ChedUltInfo.SIZE).build());
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(15, location -> {
                location.setPitch(0);

                ChedUltInfo.SOUND.TICK.play(location);

                ChedUltInfo.PARTICLE.BULLET_TRAIL_CORE.play(location);

                ChedUltInfo.PARTICLE.BULLET_TRAIL_SHAPE.play(LocationUtil.getLocationFromOffset(location, 0, -0.5, -0.6),
                        0.2, 0.12);
                ChedUltInfo.PARTICLE.BULLET_TRAIL_SHAPE.play(LocationUtil.getLocationFromOffset(location, 0, -0.7, -1.2),
                        0.16, 0.08);
                ChedUltInfo.PARTICLE.BULLET_TRAIL_SHAPE.play(LocationUtil.getLocationFromOffset(location, 0, -0.9, -1.8),
                        0.12, 0.04);

                ChedUltInfo.PARTICLE.BULLET_TRAIL_SHAPE.play(LocationUtil.getLocationFromOffset(location, 0, 0.4, 0.8),
                        0.1, 0.16);
                ChedUltInfo.PARTICLE.BULLET_TRAIL_SHAPE.play(LocationUtil.getLocationFromOffset(location, 0, 0.6, 1),
                        0.1, 0.16);
                ChedUltInfo.PARTICLE.BULLET_TRAIL_SHAPE.play(LocationUtil.getLocationFromOffset(location, 0, 0.8, 1.4),
                        0.18, 0.16);
                ChedUltInfo.PARTICLE.BULLET_TRAIL_SHAPE.play(LocationUtil.getLocationFromOffset(location, 0, 0.8, 1.6),
                        0.24, 0.16);

                ChedUltInfo.PARTICLE.BULLET_TRAIL_DECO_1.play(LocationUtil.getLocationFromOffset(location, -2.8, 1.7, 0));
                ChedUltInfo.PARTICLE.BULLET_TRAIL_DECO_1.play(LocationUtil.getLocationFromOffset(location, 2.8, 1.7, 0));

                for (int i = 0; i < 6; i++) {
                    Location loc1 = LocationUtil.getLocationFromOffset(location, 0.7 + i * 0.4, 0.3 + i * (i < 3 ? 0.2 : 0.25), 0);
                    Location loc2 = LocationUtil.getLocationFromOffset(location, -0.7 - i * 0.4, 0.3 + i * (i < 3 ? 0.2 : 0.25), 0);
                    Vector vec = VectorUtil.getSpreadedVector(getVelocity().clone().normalize(), 20);

                    ChedUltInfo.PARTICLE.BULLET_TRAIL_SHAPE.play(loc1, 0.1, 0.1 + i * 0.04);
                    ChedUltInfo.PARTICLE.BULLET_TRAIL_SHAPE.play(loc2, 0.1, 0.1 + i * 0.04);
                    ChedUltInfo.PARTICLE.BULLET_TRAIL_DECO_2.play(loc1, vec);
                    ChedUltInfo.PARTICLE.BULLET_TRAIL_DECO_2.play(loc2, vec);
                }
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> true;
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                Location loc = target.getHitboxCenter().add(0, 0.1, 0);
                new ChedUltArea().emit(loc);

                entityModule.set(new ChedUltFireFloor(loc));

                for (Location loc2 : LocationUtil.getLine(location, loc, 0.4))
                    ChedUltInfo.PARTICLE.HIT_ENTITY.play(loc2);

                ChedUltInfo.SOUND.EXPLODE.play(loc);
                ChedUltInfo.PARTICLE.EXPLODE.play(loc);

                return false;
            };
        }

        private final class ChedUltArea extends Area<Damageable> {
            private ChedUltArea() {
                super(combatUser, ChedUltInfo.SIZE, ChedUltProjectile.this.entityCondition.include(combatUser));
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                if (target.getDamageModule().damage(ChedUltProjectile.this, 0, DamageType.NORMAL, null,
                        false, false) && target instanceof CombatUser)
                    bonusScoreModule.addTarget((CombatUser) target, ChedUltInfo.KILL_SCORE_TIME_LIMIT);

                double distance = center.distance(location);
                double damage = CombatUtil.getDistantDamage(ChedUltInfo.DAMAGE, distance, ChedUltInfo.SIZE / 2.0);
                if (target.getDamageModule().damage(ChedUltProjectile.this, damage, DamageType.NORMAL, null, false, false)
                        && target instanceof Movable)
                    ((Movable) target).getMoveModule().knockback(LocationUtil.getDirection(location, location.clone().add(0, 1, 0))
                            .multiply(ChedUltInfo.KNOCKBACK));

                return !(target instanceof Barrier);
            }
        }
    }

    /**
     * 화염 지대 클래스.
     */
    public final class ChedUltFireFloor extends SummonEntity<ArmorStand> {
        private ChedUltFireFloor(@NonNull Location spawnLocation) {
            super(
                    ArmorStand.class,
                    spawnLocation,
                    combatUser.getName() + "의 화염 지대",
                    combatUser,
                    false, true,
                    Hitbox.builder(1, 1, 1).offsetY(0.5).pitchFixed().build()
            );

            addOnTick(this::onTick);
        }

        private void onTick(long i) {
            Location loc = getLocation().add(0, 0.1, 0);
            new ChedUltFireFloorArea().emit(loc);

            if (i % 4 == 0)
                ChedUltInfo.SOUND.FIRE_FLOOR_TICK.play(loc);
            ChedUltInfo.PARTICLE.FIRE_FLOOR_TICK.play(loc);

            if (i >= ChedUltInfo.FIRE_FLOOR_DURATION.toTicks())
                remove();
        }

        private final class ChedUltFireFloorArea extends Area<Damageable> {
            private ChedUltFireFloorArea() {
                super(combatUser, ChedUltInfo.SIZE, CombatUtil.EntityCondition.enemy(combatUser));
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                if (target.getDamageModule().damage(combatUser, 0, DamageType.NORMAL, null,
                        false, false)) {
                    target.getStatusEffectModule().apply(ChedUltBurning.instance, combatUser, Timespan.ofTicks(10));

                    if (target instanceof CombatUser)
                        bonusScoreModule.addTarget((CombatUser) target, ChedUltInfo.KILL_SCORE_TIME_LIMIT);
                }

                return !(target instanceof Barrier);
            }
        }
    }
}
