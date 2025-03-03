package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;

@Getter
public final class SiliaUlt extends UltimateSkill {
    /** 수정자 ID */
    private static final String MODIFIER_ID = "SiliaUlt";
    /** 활성화 완료 여부 */
    private boolean isEnabled = false;

    public SiliaUlt(@NonNull CombatUser combatUser) {
        super(combatUser, SiliaUltInfo.getInstance());
    }

    @Override
    public long getDefaultDuration() {
        return SiliaUltInfo.DURATION;
    }

    @Override
    public int getCost() {
        return SiliaUltInfo.COST;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        setDuration(-1);
        combatUser.setGlobalCooldown(Timespan.ofTicks(SiliaUltInfo.READY_DURATION));
        combatUser.getWeapon().setVisible(false);

        SiliaA3 skill3 = combatUser.getSkill(SiliaA3Info.getInstance());
        if (skill3.isCancellable())
            skill3.onCancelled();

        float yaw = combatUser.getLocation().getYaw();

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> playUseTickEffect(i, yaw), () -> {
            onCancelled();
            onReady();
        }, 1, SiliaUltInfo.READY_DURATION));
    }

    @Override
    public boolean isCancellable() {
        return !isEnabled && !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        setDuration(0);
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
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, SiliaUltInfo.SPEED);
        combatUser.getSkill(SiliaA1Info.getInstance()).setCooldown(0);

        SiliaUltInfo.SOUND.USE_READY.play(combatUser.getLocation());

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> !isDurationFinished() && !combatUser.isDead(), () -> {
            isEnabled = false;
            ((SiliaWeapon) combatUser.getWeapon()).setStrike(false);
            combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
        }, 1));
    }
}
