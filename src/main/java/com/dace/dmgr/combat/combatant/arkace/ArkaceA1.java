package com.dace.dmgr.combat.combatant.arkace;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.handler.LeftClickHandler;
import com.dace.dmgr.combat.ability.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.KnockbackModule;
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

public final class ArkaceA1 extends ActiveSkill implements LeftClickHandler {
    public ArkaceA1(@NonNull CombatUser combatUser, @NonNull ArkaceA1Info skillInfo) {
        super(combatUser, skillInfo, ArkaceA1Info.COOLDOWN, Timespan.MAX);
    }

    @Override
    protected boolean canUse() {
        return super.canUse() && isDurationFinished();
    }

    @Override
    @NonNull
    public ActionKey.Slot getSlot() {
        return ActionKey.Slot.SLOT_2;
    }

    @Override
    public void onSlot() {
        setDuration();

        combatUser.getAbilityManager().getWeapon().cancel();
        combatUser.setGlobalCooldown(ArkaceA1Info.GLOBAL_COOLDOWN);

        addActionTask(new IntervalTask(i -> {
            Location loc = combatUser.getArmLocation(MainHand.LEFT);
            new ArkaceA1Projectile().shot(loc);

            ArkaceA1Info.Effects.SHOOT.play(loc);
        }, () -> addActionTask(new DelayTask(this::cancel, 4)), 5, 3));
    }

    @Override
    public void onLeftClick() {
        onSlot();
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
    }

    private final class ArkaceA1Projectile extends Projectile<Damageable> {
        private ArkaceA1Projectile() {
            super(ArkaceA1.this, ArkaceA1Info.VELOCITY, EntityCondition.enemy(combatUser));
        }

        @Override
        protected void onHit(@NonNull Location location) {
            Location loc = location.add(0, 0.1, 0);
            new ArkaceA1Area().emit(loc);

            ArkaceA1Info.Effects.EXPLODE.play(loc);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(10, ArkaceA1Info.Effects.BULLET_TRAIL::play);
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> false;
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                if (target.getDamageModule().damage(this, ArkaceA1Info.DAMAGE_DIRECT, DamageType.NORMAL, location, false, true)) {
                    KnockbackModule.knockback(target, getVelocity(), ArkaceA1Info.KNOCKBACK);

                    if (target.isGoalTarget())
                        combatUser.addScore(ArkaceA1Info.DIRECT_HIT_SCORE);
                }

                return false;
            };
        }

        private final class ArkaceA1Area extends Area<Damageable> {
            private ArkaceA1Area() {
                super(combatUser, ArkaceA1Info.RADIUS, ArkaceA1Projectile.this.entityCondition.include(combatUser));
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                double damage = ArkaceA1Info.DISTANT_DAMAGE_EXPLODE.getDamage(center.distance(location));

                if (target.getDamageModule().damage(ArkaceA1Projectile.this, damage, DamageType.NORMAL, null, false, true)
                        && !ArkaceA1Projectile.this.getHitTargets().contains(target))
                    KnockbackModule.knockback(target, LocationUtil.getDirection(center, location.clone().add(0, 0.5, 0)), ArkaceA1Info.KNOCKBACK);

                return !(target instanceof Barrier);
            }
        }
    }
}
