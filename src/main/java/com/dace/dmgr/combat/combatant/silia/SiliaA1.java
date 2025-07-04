package com.dace.dmgr.combat.combatant.silia;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.ActiveSkill;
import com.dace.dmgr.combat.ability.weapon.Weapon;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.MeleeHitscan;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.MainHand;
import org.bukkit.util.Vector;

import java.util.HashSet;

public final class SiliaA1 extends ActiveSkill {
    public SiliaA1(@NonNull CombatUser combatUser, @NonNull SiliaA1Info skillInfo) {
        super(combatUser, skillInfo, SiliaA1Info.COOLDOWN, Timespan.MAX);
    }

    @Override
    protected boolean canUse() {
        return super.canUse() && isDurationFinished() && combatUser.getAbilityManager().getAbility(SiliaP2Info.getInstance()).isDurationFinished();
    }

    @Override
    @NonNull
    public ActionKey.Slot getSlot() {
        return ActionKey.Slot.SLOT_1;
    }

    @Override
    public void onSlot() {
        setDuration();

        combatUser.setGlobalCooldown(SiliaA1Info.DURATION);
        combatUser.playMeleeAttackAnimation(-3, Timespan.ofTicks(6), MainHand.RIGHT);

        Weapon weapon = combatUser.getAbilityManager().getWeapon();
        weapon.setCooldown(Timespan.ZERO);
        weapon.setVisible(false);

        Location location = combatUser.getEntity().getEyeLocation().subtract(0, 0.5, 0);

        SiliaA1Info.Effects.USE.play(location);

        HashSet<Damageable> targets = new HashSet<>();

        addActionTask(new IntervalTask(i -> {
            combatUser.getMoveModule().push(location.getDirection().multiply(SiliaA1Info.PUSH), true);

            new SiliaA1MeleeHitscan(targets).shot();

            combatUser.setYawAndPitch(location.getYaw(), location.getPitch());

            Location loc = combatUser.getEntity().getEyeLocation().subtract(0, 0.5, 0);

            addTask(new DelayTask(() -> SiliaA1Info.Effects.playTick(combatUser.getEntity().getEyeLocation(), loc), 1));
        }, () -> {
            forceCancel();
            combatUser.getMoveModule().push(new Vector(), true);
        }, 1, SiliaA1Info.DURATION.toTicks()));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        if (!isDurationFinished())
            setDuration(Timespan.ZERO);
        else
            setCooldown(Timespan.ZERO);

        combatUser.getAbilityManager().getWeapon().setVisible(true);
    }

    /**
     * 다른 엔티티를 죽였을 때 실행될 작업.
     *
     * @param victim 피격자
     */
    void onKill(@NonNull Damageable victim) {
        if (victim.isGoalTarget() && (!isCooldownFinished() || !isDurationFinished()))
            setCooldown(Timespan.ZERO);
    }

    private final class SiliaA1MeleeHitscan extends MeleeHitscan<Damageable> {
        private final HashSet<Damageable> targets;

        private SiliaA1MeleeHitscan(@NonNull HashSet<Damageable> targets) {
            super(combatUser, EntityCondition.enemy(combatUser), SiliaA1Info.DISTANCE, 0);
            this.targets = targets;
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(12, location -> {
                new SiliaA1Area().emit(location);
                SiliaA1Info.Effects.playBulletTrail(location, getVelocity());
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> false;
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> true;
        }

        private final class SiliaA1Area extends Area<Damageable> {
            private SiliaA1Area() {
                super(combatUser, SiliaA1Info.RADIUS, SiliaA1MeleeHitscan.this.entityCondition);
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                if (targets.add(target)) {
                    target.getDamageModule().damage(combatUser, SiliaA1Info.DAMAGE, DamageType.NORMAL, null,
                            SiliaT1.getCritMultiplier(LocationUtil.getDirection(center, location), target), true);

                    SiliaA1Info.Effects.HIT_ENTITY.play(location);
                }

                return true;
            }
        }
    }
}
