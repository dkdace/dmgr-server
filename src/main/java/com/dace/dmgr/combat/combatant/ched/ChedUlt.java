package com.dace.dmgr.combat.combatant.ched;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.HasBonusScore;
import com.dace.dmgr.combat.action.skill.Summonable;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.action.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.action.skill.module.EntityModule;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.entity.module.statuseffect.Burning;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.entity.temporary.SummonEntity;
import com.dace.dmgr.combat.entity.temporary.spawnhandler.ArmorStandSpawnHandler;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.MainHand;
import org.bukkit.util.Vector;

import java.util.function.LongConsumer;

public final class ChedUlt extends UltimateSkill implements Summonable<ChedUlt.ChedUltFireFloor>, HasBonusScore {
    /** 수정자 ID */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(-ChedUltInfo.READY_SLOW);

    /** 소환 엔티티 모듈 */
    @NonNull
    @Getter
    private final EntityModule<ChedUltFireFloor> entityModule;
    /** 보너스 점수 모듈 */
    @NonNull
    @Getter
    private final BonusScoreModule bonusScoreModule;
    /** 화염 상태 효과 */
    private final Burning burning;

    public ChedUlt(@NonNull CombatUser combatUser) {
        super(combatUser, ChedUltInfo.getInstance(), Timespan.MAX, ChedUltInfo.COST);

        this.entityModule = new EntityModule<>(this);
        this.bonusScoreModule = new BonusScoreModule(this, "궁극기 보너스", ChedUltInfo.KILL_SCORE);
        this.burning = new Burning(combatUser, ChedUltInfo.FIRE_DAMAGE_PER_SECOND, false);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        ChedP1 skillp1 = combatUser.getActionManager().getSkill(ChedP1Info.getInstance());
        return super.canUse(actionKey) && (skillp1.isDurationFinished() || skillp1.isHanging());
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        setDuration();

        combatUser.setGlobalCooldown(ChedUltInfo.READY_DURATION);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);

        ChedWeapon weapon = (ChedWeapon) combatUser.getActionManager().getWeapon();
        weapon.cancel();
        weapon.setCanShoot(false);

        ChedUltInfo.Effects.USE.play(combatUser.getLocation());

        long durationTicks = ChedUltInfo.READY_DURATION.toTicks();

        addActionTask(new IntervalTask(i -> ChedUltInfo.Effects.playUseTick(i, combatUser.getArmLocation(MainHand.RIGHT)), () -> {
            cancel();

            Location loc = combatUser.getArmLocation(MainHand.RIGHT);
            new ChedUltProjectile().shot(loc);

            ChedUltInfo.Effects.USE_READY.play(loc);

            addActionTask(new IntervalTask((LongConsumer) i -> ChedUltInfo.Effects.playUseTick(i + durationTicks, loc), 1, 20));
        }, 1, durationTicks));
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

    private final class ChedUltProjectile extends Projectile<Damageable> {
        private ChedUltProjectile() {
            super(ChedUlt.this, ChedUltInfo.VELOCITY, EntityCondition.enemy(combatUser).and(Damageable::isGoalTarget),
                    Option.builder().size(ChedUltInfo.SIZE).build());
        }

        @Override
        protected boolean canBeRemoved() {
            return false;
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(15, location -> ChedUltInfo.Effects.playBulletTrail(location, getVelocity()));
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
                    ChedUltInfo.Effects.HIT_ENTITY.play(loc2);

                ChedUltInfo.Effects.EXPLODE.play(loc);

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
                if (target.getDamageModule().damage(ChedUltProjectile.this, 0, DamageType.NORMAL, null, false, false)
                        && target.isGoalTarget())
                    bonusScoreModule.addTarget(target, ChedUltInfo.KILL_SCORE_TIME_LIMIT);

                if (target.getDamageModule().damage(ChedUltProjectile.this, ChedUltInfo.DISTANT_DAMAGE.getDamage(center.distance(location)),
                        DamageType.NORMAL, null, false, false) && target instanceof Movable) {
                    Vector dir = LocationUtil.getDirection(location, location.clone().add(0, 1, 0)).multiply(ChedUltInfo.KNOCKBACK);
                    ((Movable) target).getMoveModule().knockback(dir);
                }

                return !(target instanceof Barrier);
            }
        }
    }

    /**
     * 화염 지대 클래스.
     */
    public final class ChedUltFireFloor extends SummonEntity<ArmorStand> {
        private ChedUltFireFloor(@NonNull Location spawnLocation) {
            super(ArmorStandSpawnHandler.getInstance(), spawnLocation, combatUser.getName() + "의 화염 지대", combatUser, false);
            addOnTick(this::onTick);
        }

        private void onTick(long i) {
            Location loc = getLocation().add(0, 0.1, 0);
            new ChedUltFireFloorArea().emit(loc);

            if (i % 4 == 0)
                ChedUltInfo.Effects.FIRE_FLOOR_TICK_SOUND.play(loc);
            ChedUltInfo.Effects.FIRE_FLOOR_TICK_PARTICLE.play(loc);

            if (i >= ChedUltInfo.FIRE_FLOOR_DURATION.toTicks())
                remove();
        }

        private final class ChedUltFireFloorArea extends Area<Damageable> {
            private ChedUltFireFloorArea() {
                super(combatUser, ChedUltInfo.SIZE, EntityCondition.enemy(combatUser).and(combatEntity ->
                        Math.abs(combatEntity.getLocation().getY() - ChedUltFireFloor.this.getLocation().getY()) < ChedUltInfo.FIRE_FLOOR_HEIGHT));
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                if (target.getDamageModule().damage(combatUser, 0, DamageType.NORMAL, null, false, false)) {
                    target.getStatusEffectModule().apply(burning, Timespan.ofTicks(10));

                    if (target.isGoalTarget())
                        bonusScoreModule.addTarget(target, ChedUltInfo.KILL_SCORE_TIME_LIMIT);
                }

                return !(target instanceof Barrier);
            }
        }
    }
}
