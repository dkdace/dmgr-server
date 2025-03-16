package com.dace.dmgr.combat.combatant.silia.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
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
        combatUser.getWeapon().setVisible(false);

        SiliaA3 skill3 = combatUser.getSkill(SiliaA3Info.getInstance());
        skill3.cancel();

        float yaw = combatUser.getLocation().getYaw();

        addActionTask(new IntervalTask(i -> playUseTickEffect(i, yaw), () -> {
            cancel();
            onReady();
        }, 1, SiliaUltInfo.READY_DURATION.toTicks()));
    }

    @Override
    public boolean isCancellable() {
        return !isEnabled && !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
        combatUser.getWeapon().setVisible(true);
    }

    /**
     * 사용 시 효과를 재생한다.
     *
     * @param i   인덱스
     * @param yaw 원본 Yaw 값
     */
    private void playUseTickEffect(long i, float yaw) {
        Location loc = combatUser.getLocation().add(0, 1, 0);

        for (int j = 0; j < 6; j++) {
            long index = i * 6 + j;
            double forward = -1;
            double angle;

            if (i > 11) {
                long subIndex = (index - 12 * 6);
                forward += 0.0025 * subIndex;
                angle = index * 1.7 - subIndex * 4;
                loc.setPitch(index * 4 + subIndex);
            } else {
                angle = index * 1.7;
                loc.setPitch(index * 4);
            }
            loc.setYaw((float) (yaw + angle));

            for (int k = 0; k < 3; k++) {
                Location loc2 = LocationUtil.getLocationFromOffset(loc, 0, 0, forward - 0.4 * k);
                if (k != 2)
                    SiliaUltInfo.PARTICLE.USE_TICK_CORE.play(loc2);
                else
                    SiliaUltInfo.PARTICLE.USE_TICK_DECO.play(loc2);
            }
        }
    }

    /**
     * 시전 완료 시 실행할 작업.
     */
    private void onReady() {
        setDuration();
        isEnabled = true;
        ((SiliaWeapon) combatUser.getWeapon()).setStrike(true);
        combatUser.getWeapon().setVisible(true);
        combatUser.getWeapon().setGlowing(true);
        combatUser.getWeapon().setDurability(SiliaWeaponInfo.RESOURCE.EXTENDED);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);
        combatUser.getSkill(SiliaA1Info.getInstance()).setCooldown(Timespan.ZERO);

        SiliaUltInfo.SOUND.USE_READY.play(combatUser.getLocation());

        addActionTask(new IntervalTask(i -> !isDurationFinished() && !combatUser.isDead(), () -> {
            isEnabled = false;
            ((SiliaWeapon) combatUser.getWeapon()).setStrike(false);
            combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER);
        }, 1));
    }
}
