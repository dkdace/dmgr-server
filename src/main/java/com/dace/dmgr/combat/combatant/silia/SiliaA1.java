package com.dace.dmgr.combat.combatant.silia;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.util.VectorUtil;
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
    public SiliaA1(@NonNull CombatUser combatUser) {
        super(combatUser, SiliaA1Info.getInstance(), SiliaA1Info.COOLDOWN, Timespan.MAX, 0);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_1};
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && combatUser.getActionManager().getSkill(SiliaP2Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();

        combatUser.setGlobalCooldown(SiliaA1Info.DURATION);
        combatUser.playMeleeAttackAnimation(-3, Timespan.ofTicks(6), MainHand.RIGHT);

        Weapon weapon = combatUser.getActionManager().getWeapon();
        weapon.setCooldown(Timespan.ZERO);
        weapon.setVisible(false);

        Location location = combatUser.getEntity().getEyeLocation().subtract(0, 0.5, 0);

        SiliaA1Info.Sounds.USE.play(location);

        HashSet<Damageable> targets = new HashSet<>();

        addActionTask(new IntervalTask(i -> {
            combatUser.getMoveModule().push(location.getDirection().multiply(SiliaA1Info.PUSH), true);

            new SiliaA1Attack(targets).shot();

            combatUser.setYawAndPitch(location.getYaw(), location.getPitch());

            Location loc = combatUser.getEntity().getEyeLocation().subtract(0, 0.5, 0);

            addTask(new DelayTask(() -> {
                Location loc2 = combatUser.getEntity().getEyeLocation().subtract(0, 0.5, 0);
                for (Location loc3 : LocationUtil.getLine(loc, loc2, 0.3))
                    SiliaA1Info.Particles.TICK.play(loc3);
            }, 1));
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

        combatUser.getActionManager().getWeapon().setVisible(true);
    }

    private final class SiliaA1Attack extends Hitscan<Damageable> {
        private final HashSet<Damageable> targets;

        private SiliaA1Attack(@NonNull HashSet<Damageable> targets) {
            super(combatUser, EntityCondition.enemy(combatUser), Option.builder().maxDistance(SiliaA1Info.DISTANCE).build());
            this.targets = targets;
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(12, location -> {
                Location loc = LocationUtil.getLocationFromOffset(location, 0, -0.2, 1);
                Vector vector = VectorUtil.getPitchAxis(loc).multiply(1.5);
                Vector axis = VectorUtil.getYawAxis(loc);

                for (int i = 0; i < 12; i++) {
                    Vector vec = VectorUtil.getRotatedVector(vector, axis, 90 + 15 * (i - 5.5));

                    for (int j = 0; j < 3; j++) {
                        Location loc2 = LocationUtil.getLocationFromOffset(loc.clone().add(vec), 0, 0.3 - j * 0.3, 0);
                        SiliaA1Info.Particles.BULLET_TRAIL_CORE.play(loc2);

                        if ((i == 0 || i == 11) && j == 1) {
                            Vector vec2 = VectorUtil.getSpreadedVector(getVelocity().normalize(), 10);
                            SiliaA1Info.Particles.BULLET_TRAIL_DECO.play(loc2, vec2);
                        }
                    }
                }

                new SiliaA1Area().emit(location);
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
                super(combatUser, SiliaA1Info.RADIUS, SiliaA1Attack.this.entityCondition);
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

                    SiliaA1Info.Particles.HIT_ENTITY.play(location);
                }

                return !(target instanceof Barrier);
            }
        }
    }
}
