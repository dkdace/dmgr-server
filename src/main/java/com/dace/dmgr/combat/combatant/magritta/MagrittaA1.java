package com.dace.dmgr.combat.combatant.magritta;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.combatuser.AbilityManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.statuseffect.Burning;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.MainHand;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

public final class MagrittaA1 extends ActiveSkill {
    /** 화염 상태 효과 */
    private final Burning burning;

    public MagrittaA1(@NonNull CombatUser combatUser, @NonNull MagrittaA1Info skillInfo) {
        super(combatUser, skillInfo, MagrittaA1Info.COOLDOWN, Timespan.MAX);
        this.burning = new Burning(combatUser, MagrittaA1Info.FIRE_DAMAGE_PER_SECOND, true);
    }

    @Override
    @NonNull
    public Set<@NonNull ActionKey> getDefaultActionKeys() {
        return EnumSet.of(ActionKey.SLOT_1);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        AbilityManager abilityManager = combatUser.getAbilityManager();
        return super.canUse(actionKey) && abilityManager.getSkill(MagrittaA2Info.getInstance()).isDurationFinished()
                && abilityManager.getSkill(MagrittaUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();

        combatUser.getAbilityManager().getWeapon().cancel();
        combatUser.setGlobalCooldown(MagrittaA1Info.READY_DURATION);

        MagrittaA1Info.Effects.USE.play(combatUser.getLocation());

        addActionTask(new DelayTask(() -> {
            cancel();

            Location loc = combatUser.getArmLocation(MainHand.RIGHT);
            new MagrittaA1Projectile().shot(loc);

            MagrittaA1Info.Effects.USE_READY.play(loc);
        }, MagrittaA1Info.READY_DURATION.toTicks()));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
    }

    private final class MagrittaA1Projectile extends Projectile<Damageable> {
        private MagrittaA1Projectile() {
            super(MagrittaA1.this, MagrittaA1Info.VELOCITY, EntityCondition.enemy(combatUser));
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return IntervalHandler
                    .chain(createGravityIntervalHandler())
                    .next(createPeriodIntervalHandler(7, MagrittaA1Info.Effects.BULLET_TRAIL::play));
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                onStuck(location, null);
                return false;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                target.getDamageModule().damage(this, MagrittaA1Info.DAMAGE_DIRECT, DamageType.NORMAL, location, false, true);
                onStuck(location, target);

                return false;
            };
        }

        /**
         * 폭탄 부착 시 실행할 작업.
         *
         * @param location 부착 위치
         * @param target   부착 대상
         */
        private void onStuck(@NonNull Location location, @Nullable Damageable target) {
            MagrittaA1Info.Effects.STUCK.play(location);

            if (target != null) {
                combatUser.getUser().sendTitle("§b§l부착", "", Timespan.ZERO, Timespan.ofTicks(5), Timespan.ofTicks(10));

                if (target.isGoalTarget())
                    combatUser.addScore("부착", MagrittaA1Info.STUCK_SCORE);
            }

            addTask(new IntervalTask(i -> {
                Location loc = location.clone();

                if (target == null)
                    MagrittaA1Info.Effects.STUCK_TICK_PARTICLE.play(loc);
                else {
                    if (target.isRemoved() || target instanceof CombatUser && ((CombatUser) target).isDead())
                        return false;

                    loc = target.getCenterLocation();
                }

                if (i % 2 == 0)
                    MagrittaA1Info.Effects.STUCK_TICK_SOUND.play(loc);

                return true;
            }, isCancelled -> {
                Location loc = (target == null ? location.clone() : target.getHitboxCenter()).add(0, 0.1, 0);
                new MagrittaA1Area().emit(loc);

                MagrittaA1Info.Effects.EXPLODE.play(loc);
            }, 1, MagrittaA1Info.EXPLODE_DURATION.toTicks()));
        }

        private final class MagrittaA1Area extends Area<Damageable> {
            private MagrittaA1Area() {
                super(combatUser, MagrittaA1Info.RADIUS, EntityCondition.enemy(combatUser).include(combatUser));
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                double distance = center.distance(location);

                if (target.getDamageModule().damage(MagrittaA1Projectile.this, MagrittaA1Info.DISTANT_DAMAGE_EXPLODE.getDamage(distance),
                        DamageType.NORMAL, null, false, true)) {
                    target.getStatusEffectModule().apply(burning, MagrittaA1Info.DISTANT_FIRE_DURATION.getTimespan(distance));

                    if (target instanceof Movable) {
                        Vector dir = LocationUtil.getDirection(center, location.add(0, 0.5, 0)).multiply(MagrittaA1Info.KNOCKBACK);
                        ((Movable) target).getKnockbackModule().knockback(dir);
                    }

                    MagrittaT1Util.addValue(combatUser, target);
                }

                return !(target instanceof Barrier);
            }
        }
    }
}
