package com.dace.dmgr.combat.combatant.magritta.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.module.statuseffect.Burning;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.MainHand;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public final class MagrittaA1 extends ActiveSkill {
    public MagrittaA1(@NonNull CombatUser combatUser) {
        super(combatUser, MagrittaA1Info.getInstance(), MagrittaA1Info.COOLDOWN, Timespan.MAX, 0);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_1};
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && combatUser.getSkill(MagrittaA2Info.getInstance()).isDurationFinished()
                && combatUser.getSkill(MagrittaUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();

        combatUser.getWeapon().cancel();
        combatUser.setGlobalCooldown(MagrittaA1Info.READY_DURATION);

        MagrittaA1Info.SOUND.USE.play(combatUser.getLocation());

        addActionTask(new DelayTask(() -> {
            cancel();

            Location loc = combatUser.getArmLocation(MainHand.RIGHT);
            new MagrittaA1Projectile().shot(loc);

            CombatEffectUtil.THROW_SOUND.play(loc, 1, 0.5);
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

    /**
     * 화염 상태 효과 클래스.
     */
    private static final class MagrittaA1Burning extends Burning {
        private static final MagrittaA1Burning instance = new MagrittaA1Burning();

        private MagrittaA1Burning() {
            super(MagrittaA1Info.FIRE_DAMAGE_PER_SECOND, true);
        }
    }

    private final class MagrittaA1Projectile extends Projectile<Damageable> {
        private MagrittaA1Projectile() {
            super(combatUser, MagrittaA1Info.VELOCITY, CombatUtil.EntityCondition.enemy(combatUser));
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return IntervalHandler
                    .chain(createGravityIntervalHandler())
                    .next(createPeriodIntervalHandler(7, MagrittaA1Info.PARTICLE.BULLET_TRAIL::play));
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
            MagrittaA1Info.SOUND.STUCK.play(location);

            if (target != null) {
                combatUser.getUser().sendTitle("§b§l부착", "", Timespan.ZERO, Timespan.ofTicks(5), Timespan.ofTicks(10));

                if (target instanceof CombatUser)
                    combatUser.addScore("부착", MagrittaA1Info.STUCK_SCORE);
            }

            addTask(new IntervalTask(i -> {
                Location loc = location.clone();

                if (target == null)
                    MagrittaA1Info.PARTICLE.TICK.play(loc);
                else {
                    if (target.isRemoved() || target instanceof CombatUser && ((CombatUser) target).isDead())
                        return false;

                    loc = target.getCenterLocation();
                }

                if (i % 2 == 0)
                    MagrittaA1Info.SOUND.TICK.play(loc);

                return true;
            }, isCancelled -> {
                Location loc = (target == null ? location.clone() : target.getHitboxCenter()).add(0, 0.1, 0);
                new MagrittaA1Area().emit(loc);

                MagrittaA1Info.SOUND.EXPLODE.play(loc);
                MagrittaA1Info.PARTICLE.EXPLODE.play(loc);
            }, 1, MagrittaA1Info.EXPLODE_DURATION.toTicks()));
        }

        private final class MagrittaA1Area extends Area<Damageable> {
            private MagrittaA1Area() {
                super(combatUser, MagrittaA1Info.RADIUS, CombatUtil.EntityCondition.enemy(combatUser).include(combatUser));
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                double distance = center.distance(location);
                double damage = CombatUtil.getDistantDamage(MagrittaA1Info.DAMAGE_EXPLODE, distance, radius / 2.0);
                Timespan burningDuration = Timespan.ofTicks((long) CombatUtil.getDistantDamage(MagrittaA1Info.FIRE_DURATION.toTicks(), distance,
                        radius / 2.0));

                if (target.getDamageModule().damage(MagrittaA1Projectile.this, damage, DamageType.NORMAL, null, false, true)) {
                    target.getStatusEffectModule().apply(MagrittaA1Burning.instance, combatUser, burningDuration);

                    if (target instanceof Movable) {
                        Vector dir = LocationUtil.getDirection(center, location.add(0, 0.5, 0)).multiply(MagrittaA1Info.KNOCKBACK);
                        ((Movable) target).getMoveModule().knockback(dir);
                    }

                    MagrittaT1.addShreddingValue(combatUser, target);
                }

                return !(target instanceof Barrier);
            }
        }
    }
}
