package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.inventory.MainHand;
import org.bukkit.util.Vector;

import java.util.HashSet;

public final class QuakerWeapon extends AbstractWeapon {
    /** 휘두르는 방향의 시계 방향 여부 */
    private boolean isClockwise = true;

    public QuakerWeapon(@NonNull CombatUser combatUser) {
        super(combatUser, QuakerWeaponInfo.getInstance());
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.LEFT_CLICK};
    }

    @Override
    public long getDefaultCooldown() {
        return QuakerWeaponInfo.COOLDOWN;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && combatUser.getSkill(QuakerA1Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setCooldown();
        setVisible(false);
        combatUser.setGlobalCooldown(Timespan.ofTicks(QuakerWeaponInfo.GLOBAL_COOLDOWN));
        combatUser.playMeleeAttackAnimation(-10, Timespan.ofTicks(15), isClockwise ? MainHand.RIGHT : MainHand.LEFT);

        TaskUtil.addTask(taskRunner, new DelayTask(() -> {
            isClockwise = !isClockwise;
            HashSet<Damageable> targets = new HashSet<>();

            int delay = 0;
            for (int i = 0; i < 8; i++) {
                int index = i;

                if (i == 1)
                    delay += 2;
                else if (i == 2 || i == 4 || i == 6 || i == 7)
                    delay += 1;

                TaskUtil.addTask(taskRunner, new DelayTask(() -> {
                    Location loc = combatUser.getEntity().getEyeLocation();
                    Vector vector = VectorUtil.getPitchAxis(loc);
                    Vector axis = VectorUtil.getYawAxis(loc);

                    Vector vec = VectorUtil.getRotatedVector(vector, axis, (isClockwise ? (index + 1) * 20 : 180 - (index + 1) * 20));
                    new QuakerWeaponAttack(targets).shot(loc, vec);

                    combatUser.addYawAndPitch(isClockwise ? 0.8 : -0.8, 0.1);
                    if (index % 2 == 0)
                        QuakerWeaponInfo.SOUND.USE.play(loc.add(vec));
                    if (index == 7) {
                        combatUser.addYawAndPitch(isClockwise ? -1 : 1, -0.7);
                        TaskUtil.addTask(taskRunner, new DelayTask(this::onCancelled, 4));
                    }
                }, delay));
            }
        }, 2));
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        setVisible(true);
    }

    private final class QuakerWeaponAttack extends Hitscan<Damageable> {
        private final HashSet<Damageable> targets;

        private QuakerWeaponAttack(@NonNull HashSet<Damageable> targets) {
            super(combatUser, CombatUtil.EntityCondition.enemy(combatUser), Option.builder().size(QuakerWeaponInfo.SIZE)
                    .maxDistance(QuakerWeaponInfo.DISTANCE).build());
            this.targets = targets;
        }

        @Override
        protected void onHit(@NonNull Location location) {
            QuakerWeaponInfo.SOUND.HIT.play(location);
        }

        @Override
        protected void onDestroy(@NonNull Location location) {
            Location loc = LocationUtil.getLocationFromOffset(location, 0, -0.3, 0);
            QuakerWeaponInfo.PARTICLE.BULLET_TRAIL_DECO.play(loc);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(6, location -> {
                if (getTravelDistance() <= 1)
                    return;

                Location loc = LocationUtil.getLocationFromOffset(location, 0, -0.3, 0);
                QuakerWeaponInfo.PARTICLE.BULLET_TRAIL_CORE.play(loc);
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                CombatEffectUtil.playHitBlockParticle(location, hitBlock, 2);
                CombatEffectUtil.playHitBlockSound(location, hitBlock, 1);

                return false;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                if (targets.add(target)) {
                    if (target.getDamageModule().damage(combatUser, QuakerWeaponInfo.DAMAGE, DamageType.NORMAL, location, false, true)
                            && target instanceof Movable)
                        ((Movable) target).getMoveModule().knockback(VectorUtil.getPitchAxis(combatUser.getLocation())
                                .multiply(isClockwise ? -QuakerWeaponInfo.KNOCKBACK : QuakerWeaponInfo.KNOCKBACK));

                    QuakerWeaponInfo.PARTICLE.HIT_ENTITY.play(location);
                    QuakerWeaponInfo.SOUND.HIT_ENTITY.play(location);
                }

                return true;
            };
        }
    }
}
