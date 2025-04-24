package com.dace.dmgr.combat.combatant.neace;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Getter(AccessLevel.PACKAGE)
public final class NeaceUlt extends UltimateSkill {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(-NeaceUltInfo.READY_SLOW);
    /** 활성화 완료 여부 */
    private boolean isEnabled = false;

    public NeaceUlt(@NonNull CombatUser combatUser) {
        super(combatUser, NeaceUltInfo.getInstance(), NeaceUltInfo.DURATION, NeaceUltInfo.COST);
    }

    @Override
    @Nullable
    public String getActionBarString() {
        return (isDurationFinished() || !isEnabled) ? null : ActionBarStringUtil.getDurationBar(this);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && combatUser.getSkill(NeaceA3Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        setDuration(Timespan.MAX);

        combatUser.setGlobalCooldown(NeaceUltInfo.READY_DURATION);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);

        NeaceUltInfo.SOUND.USE.play(combatUser.getLocation());

        EffectManager effectManager = new EffectManager();

        addActionTask(new IntervalTask(i -> effectManager.playEffect(), () -> {
            cancel();

            isEnabled = true;

            setDuration();
            combatUser.getDamageModule().heal(combatUser, combatUser.getDamageModule().getMaxHealth(), false);

            NeaceUltInfo.SOUND.USE_READY.play(combatUser.getLocation());
            NeaceUltInfo.PARTICLE.USE_READY.play(combatUser.getLocation());

            addActionTask(new IntervalTask(i -> {
                Location loc = combatUser.getEntity().getEyeLocation();
                new NeaceUltArea().emit(loc);

                playTickEffect(i);
                NeaceWeaponInfo.SOUND.USE_HEAL.play(combatUser.getLocation());
            }, 1, NeaceUltInfo.DURATION.toTicks()));
        }, 1, NeaceUltInfo.READY_DURATION.toTicks()));
    }

    @Override
    protected void onDurationFinished() {
        super.onDurationFinished();
        isEnabled = false;
    }

    @Override
    public boolean isCancellable() {
        return (!isEnabled || combatUser.isDead()) && !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER);
    }

    /**
     * 사용 중 효과를 재생한다.
     *
     * @param i 인덱스
     */
    private void playTickEffect(long i) {
        Location loc = combatUser.getLocation();
        loc.setYaw(0);
        loc.setPitch(0);
        Vector vector = VectorUtil.getRollAxis(loc).multiply(1.5);
        Vector axis = VectorUtil.getYawAxis(loc);

        long angle = i * 5;
        for (int j = 0; j < 6; j++) {
            angle += 360 / 3;
            Vector vec = VectorUtil.getRotatedVector(vector, axis, j < 3 ? angle : -angle);

            NeaceUltInfo.PARTICLE.TICK.play(loc.clone().add(vec));
        }
    }

    /**
     * 효과를 재생하는 클래스.
     */
    @NoArgsConstructor
    private final class EffectManager {
        private int index = 0;
        private int angle = 0;
        private double distance = 0;
        private double up = 0;

        /**
         * 효과를 재생한다.
         */
        private void playEffect() {
            Location loc = combatUser.getLocation().add(0, 0.1, 0);
            loc.setYaw(0);
            loc.setPitch(0);
            Vector vector = VectorUtil.getRollAxis(loc);
            Vector axis = VectorUtil.getYawAxis(loc);

            for (int j = 0; j < 3; j++) {
                angle += index > 9 ? -31 : 7;

                if (index > 9)
                    up += 0.2;
                else
                    distance += 0.35;

                for (int k = 0; k < 8; k++) {
                    angle += 360 / 4;
                    Vector vec = VectorUtil.getRotatedVector(vector, axis, k < 4 ? angle : -angle).multiply(distance);
                    Location loc2 = loc.clone().add(vec).add(0, up, 0);

                    NeaceUltInfo.PARTICLE.USE_TICK.play(loc2);
                }
            }

            index++;
        }
    }

    private final class NeaceUltArea extends Area<Healable> {
        private NeaceUltArea() {
            super(combatUser, NeaceWeaponInfo.HEAL.MAX_DISTANCE, EntityCondition.team(combatUser));
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Healable target) {
            ((NeaceWeapon) combatUser.getWeapon()).healTarget(target);
            return true;
        }
    }
}
