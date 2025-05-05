package com.dace.dmgr.combat.combatant.arkace;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
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

public final class ArkaceA1 extends ActiveSkill {
    public ArkaceA1(@NonNull CombatUser combatUser) {
        super(combatUser, ArkaceA1Info.getInstance(), ArkaceA1Info.COOLDOWN, Timespan.MAX, 1);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_2, ActionKey.LEFT_CLICK};
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();

        combatUser.getActionManager().getWeapon().cancel();
        combatUser.setGlobalCooldown(ArkaceA1Info.GLOBAL_COOLDOWN);

        addActionTask(new IntervalTask(i -> {
            Location loc = combatUser.getArmLocation(MainHand.LEFT);
            new ArkaceA1Projectile().shot(loc);

            ArkaceA1Info.Sounds.USE.play(loc);
        }, () -> addActionTask(new DelayTask(this::cancel, 4)), 5, 3));
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

            ArkaceA1Info.Sounds.EXPLODE.play(loc);
            ArkaceA1Info.Particles.EXPLODE.play(loc);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(10, ArkaceA1Info.Particles.BULLET_TRAIL::play);
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
                    if (target instanceof Movable) {
                        Vector dir = getVelocity().normalize().multiply(ArkaceA1Info.KNOCKBACK);
                        ((Movable) target).getMoveModule().knockback(dir);
                    }

                    if (target.isGoalTarget())
                        combatUser.addScore("미사일 직격", ArkaceA1Info.DIRECT_HIT_SCORE);
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
                double damage = CombatUtil.getDistantDamage(ArkaceA1Info.DAMAGE_EXPLODE, center.distance(location), radius / 2.0);

                if (target.getDamageModule().damage(ArkaceA1Projectile.this, damage, DamageType.NORMAL, null, false, true)
                        && target instanceof Movable && isNotHit(target)) {
                    Vector dir = LocationUtil.getDirection(center, location.add(0, 0.5, 0)).multiply(ArkaceA1Info.KNOCKBACK);
                    ((Movable) target).getMoveModule().knockback(dir);
                }

                return !(target instanceof Barrier);
            }
        }
    }
}
