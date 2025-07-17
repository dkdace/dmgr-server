package com.dace.dmgr.combat.combatant.silia;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.Trait;
import com.dace.dmgr.combat.ability.weapon.Weapon;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.combatuser.AbilityManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.interaction.MeleeHitscan;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.inventory.MainHand;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.function.IntConsumer;

public final class SiliaT2 extends Trait {
    /** 일격 사용 가능 여부 */
    private boolean isStrike = false;

    public SiliaT2(@NonNull CombatUser combatUser, @NonNull SiliaT2Info traitInfo) {
        super(combatUser, traitInfo);
    }

    /**
     * 일격을 사용한다.
     *
     * @param isOpposite 검기 방향의 반대 방향 여부
     * @return 사용 성공 여부
     */
    boolean useStrike(boolean isOpposite) {
        if (!isStrike)
            return false;

        AbilityManager abilityManager = combatUser.getAbilityManager();
        Weapon weapon = abilityManager.getWeapon();

        if (!abilityManager.getAbility(SiliaUltInfo.getInstance()).isDurationFinished())
            weapon.setCooldown(SiliaUltInfo.STRIKE_COOLDOWN);

        combatUser.setGlobalCooldown(SiliaT2Info.GLOBAL_COOLDOWN);
        combatUser.playMeleeAttackAnimation(-2, Timespan.ofTicks(6), isOpposite ? MainHand.LEFT : MainHand.RIGHT);

        weapon.setVisible(false);

        HashSet<Damageable> targets = new HashSet<>();

        IntConsumer onIndex = i -> {
            Location loc = combatUser.getEntity().getEyeLocation();
            Vector vector = VectorUtil.getPitchAxis(loc);
            Vector axis = VectorUtil.getYawAxis(loc);

            double angle = 16 * (i - 3.5);
            Vector vec = VectorUtil.getRotatedVector(vector, axis, (isOpposite ? 90 + angle : 90 - angle));
            vec = VectorUtil.getRotatedVector(vec, VectorUtil.getRollAxis(loc), isOpposite ? 30 : -30);

            new SiliaT2MeleeHitscan(targets).shot(loc, vec);

            combatUser.addYawAndPitch(isOpposite ? 0.5 : -0.5, 0.15);
            if (i < 3)
                SiliaT2Info.Effects.USE.apply(i).play(loc.add(vec));
            if (i == 7) {
                combatUser.addYawAndPitch(isOpposite ? -0.7 : 0.7, -0.85);
                weapon.cancel();
            }
        };

        int delay = 0;
        int[] delays = {0, 1, 1, 0, 0, 0, 1, 1};

        for (int i = 0; i < delays.length; i++) {
            int index = i;
            delay += delays[i];

            weapon.addActionTask(new DelayTask(() -> onIndex.accept(index), delay));
        }

        return true;
    }

    /**
     * 일격 사용 가능 여부를 설정한다.
     *
     * @param isStrike 일격 사용 가능 여부
     */
    void setStrike(boolean isStrike) {
        this.isStrike = isStrike;

        Weapon weapon = combatUser.getAbilityManager().getWeapon();
        weapon.setGlowing(isStrike);
        weapon.setDurability(isStrike ? SiliaWeaponInfo.Resource.EXTENDED : SiliaWeaponInfo.Resource.DEFAULT);
    }

    private final class SiliaT2MeleeHitscan extends MeleeHitscan<Damageable> {
        private final HashSet<Damageable> targets;

        private SiliaT2MeleeHitscan(@NonNull HashSet<Damageable> targets) {
            super(combatUser, EntityCondition.enemy(combatUser), SiliaT2Info.DISTANCE, SiliaT2Info.SIZE);
            this.targets = targets;
        }

        @Override
        protected void onHit(@NonNull Location location) {
            SiliaT2Info.Effects.HIT.play(location);
        }

        @Override
        protected void onDestroy(@NonNull Location location, boolean isForce) {
            Location loc = LocationUtil.getLocationFromOffset(location, 0, -0.3, 0);
            SiliaT2Info.Effects.BULLET_TRAIL_2.play(loc);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(5, location -> {
                if (getTravelDistance() <= 1)
                    return;

                Location loc = LocationUtil.getLocationFromOffset(location, 0, -0.3, 0);
                SiliaT2Info.Effects.BULLET_TRAIL_1.play(loc);
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                SiliaT2Info.Effects.HIT_BLOCK.apply(hitBlock).play(location);
                return false;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                if (targets.add(target)) {
                    if (target.getDamageModule().damage(combatUser, SiliaT2Info.DAMAGE, DamageType.NORMAL, location,
                            SiliaT1.getCritMultiplier(combatUser.getLocation().getDirection(), target), true)) {

                        if (target instanceof Movable)
                            ((Movable) target).getKnockbackModule().knockback(combatUser.getLocation().getDirection(), SiliaT2Info.KNOCKBACK);

                        if (combatUser.getAbilityManager().getAbility(SiliaUltInfo.getInstance()).isDurationFinished() && target.isGoalTarget())
                            combatUser.addScore(SiliaT2Info.DAMAGE_SCORE);
                    }

                    SiliaT2Info.Effects.HIT_ENTITY.play(location);
                }

                return true;
            };
        }
    }
}
