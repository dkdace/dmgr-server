package com.dace.dmgr.combat.combatant.silia;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

public final class SiliaUlt extends UltimateSkill {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(SiliaUltInfo.SPEED);
    /** 활성화 완료 여부 */
    private boolean isEnabled = false;

    public SiliaUlt(@NonNull CombatUser combatUser) {
        super(combatUser, SiliaUltInfo.getInstance(), SiliaUltInfo.DURATION, SiliaUltInfo.COST);
    }

    @Override
    @Nullable
    public String getActionBarString() {
        return (isDurationFinished() || !isEnabled) ? null : ActionBarStringUtil.getDurationBar(this);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        setDuration(Timespan.MAX);
        combatUser.setGlobalCooldown(SiliaUltInfo.READY_DURATION);

        SiliaWeapon weapon = (SiliaWeapon) combatUser.getWeapon();
        weapon.setVisible(false);

        combatUser.getSkill(SiliaA3Info.getInstance()).cancel();

        float yaw = combatUser.getLocation().getYaw();
        EffectManager effectManager = new EffectManager();

        addActionTask(new IntervalTask(i -> effectManager.playEffect(yaw), () -> {
            cancel();

            isEnabled = true;

            setDuration();
            combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);

            combatUser.getSkill(SiliaA1Info.getInstance()).setCooldown(Timespan.ZERO);

            weapon.setStrike(true);
            weapon.setVisible(true);

            SiliaUltInfo.SOUND.USE_READY.play(combatUser.getLocation());
        }, 1, SiliaUltInfo.READY_DURATION.toTicks()));
    }

    @Override
    protected void onDurationFinished() {
        super.onDurationFinished();

        isEnabled = false;

        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER);
        ((SiliaWeapon) combatUser.getWeapon()).setStrike(false);
    }

    @Override
    public boolean isCancellable() {
        return (!isEnabled || combatUser.isDead()) && !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
        combatUser.getWeapon().setVisible(true);
    }

    /**
     * 효과를 재생하는 클래스.
     */
    @NoArgsConstructor
    private final class EffectManager {
        private int index = 0;
        private float angle = 0;
        private float pitch = 0;
        private double forward = -1;

        /**
         * 효과를 재생한다.
         *
         * @param yaw 원본 Yaw 값
         */
        private void playEffect(float yaw) {
            Location loc = combatUser.getLocation().add(0, 1, 0);

            for (int i = 0; i < 6; i++) {
                if (index > 11) {
                    forward += 0.0025;
                    angle -= 2.3F;
                    pitch += 5;
                } else {
                    angle += 1.7F;
                    pitch += 4;
                }

                loc.setYaw(yaw + angle);
                loc.setPitch(pitch);

                for (int j = 0; j < 3; j++) {
                    Location loc2 = LocationUtil.getLocationFromOffset(loc, 0, 0, forward - 0.4 * j);

                    if (j == 2)
                        SiliaUltInfo.PARTICLE.USE_TICK_DECO.play(loc2);
                    else
                        SiliaUltInfo.PARTICLE.USE_TICK_CORE.play(loc2);
                }
            }

            index++;
        }
    }
}
