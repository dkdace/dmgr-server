package com.dace.dmgr.combat.combatant.quaker;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.DelayTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.inventory.MainHand;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.function.IntConsumer;

public final class QuakerWeapon extends AbstractWeapon {
    /** 휘두르는 방향의 반대 방향 여부 */
    private boolean isOpposite = true;

    public QuakerWeapon(@NonNull CombatUser combatUser) {
        super(combatUser, QuakerWeaponInfo.getInstance(), QuakerWeaponInfo.COOLDOWN);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.LEFT_CLICK};
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && combatUser.getSkill(QuakerA1Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setCooldown();
        setVisible(false);

        combatUser.setGlobalCooldown(QuakerWeaponInfo.GLOBAL_COOLDOWN);

        use(false);
        addActionTask(new DelayTask(this::cancel, 12));
    }

    /**
     * 기본 무기를 사용한다.
     *
     * @param isUlt 궁극기 여부
     */
    void use(boolean isUlt) {
        isOpposite = isUlt || !isOpposite;

        combatUser.playMeleeAttackAnimation(-10, Timespan.ofTicks(15), isOpposite ? MainHand.LEFT : MainHand.RIGHT);

        HashSet<Damageable> targets = new HashSet<>();

        IntConsumer onIndex = i -> {
            Location loc = combatUser.getEntity().getEyeLocation();
            Vector vector = VectorUtil.getPitchAxis(loc);
            Vector axis = VectorUtil.getYawAxis(loc);

            int angle = (i + 1) * 20;
            Vector vec = VectorUtil.getRotatedVector(vector, axis, isOpposite ? angle : 180 - angle);

            new QuakerWeaponAttack(targets, isUlt).shot(loc, vec);

            combatUser.addYawAndPitch(isOpposite ? 0.8 : -0.8, 0.1);
            if (i % 2 == 0)
                QuakerWeaponInfo.Sounds.USE.play(loc.add(vec));
            if (i == 7)
                combatUser.addYawAndPitch(isOpposite ? -1 : 1, -0.7);
        };

        int delay = 0;
        int[] delays = {2, 2, 1, 0, 1, 0, 1, 1};

        for (int i = 0; i < delays.length; i++) {
            int index = i;
            delay += delays[i];

            addActionTask(new DelayTask(() -> onIndex.accept(index), delay));
        }
    }

    @Override
    protected void onCancelled() {
        setVisible(true);
    }

    private final class QuakerWeaponAttack extends Hitscan<Damageable> {
        private final HashSet<Damageable> targets;
        private final boolean isUlt;

        private QuakerWeaponAttack(@NonNull HashSet<Damageable> targets, boolean isUlt) {
            super(combatUser, EntityCondition.enemy(combatUser), Option.builder().size(QuakerWeaponInfo.SIZE)
                    .maxDistance(QuakerWeaponInfo.DISTANCE).build());

            this.targets = targets;
            this.isUlt = isUlt;
        }

        @Override
        protected void onHit(@NonNull Location location) {
            if (!isUlt)
                QuakerWeaponInfo.Sounds.HIT.play(location);
        }

        @Override
        protected void onDestroy(@NonNull Location location) {
            Location loc = LocationUtil.getLocationFromOffset(location, 0, -0.3, 0);
            QuakerWeaponInfo.Particles.BULLET_TRAIL_DECO.play(loc);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(6, location -> {
                if (getTravelDistance() <= 1)
                    return;

                Location loc = LocationUtil.getLocationFromOffset(location, 0, -0.3, 0);
                QuakerWeaponInfo.Particles.BULLET_TRAIL_CORE.play(loc);
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                if (!isUlt) {
                    CombatEffectUtil.playHitBlockParticle(location, hitBlock, 2);
                    CombatEffectUtil.playHitBlockSound(location, hitBlock, 1);
                }

                return false;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                if (!isUlt && targets.add(target)) {
                    if (target.getDamageModule().damage(combatUser, QuakerWeaponInfo.DAMAGE, DamageType.NORMAL, location, false, true)
                            && target instanceof Movable) {
                        Vector dir = VectorUtil.getPitchAxis(combatUser.getLocation())
                                .multiply(isOpposite ? -QuakerWeaponInfo.KNOCKBACK : QuakerWeaponInfo.KNOCKBACK);
                        ((Movable) target).getMoveModule().knockback(dir);
                    }

                    QuakerWeaponInfo.Particles.HIT_ENTITY.play(location);
                    QuakerWeaponInfo.Sounds.HIT_ENTITY.play(location);
                }

                return true;
            };
        }
    }
}
