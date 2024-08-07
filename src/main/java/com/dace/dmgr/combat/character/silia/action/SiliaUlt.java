package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;

@Getter
public final class SiliaUlt extends UltimateSkill {
    /** 수정자 ID */
    private static final String MODIFIER_ID = "SiliaUlt";
    /** 일격 활성화 완료 여부 */
    private boolean isEnabled = false;

    SiliaUlt(@NonNull CombatUser combatUser) {
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
    public boolean canUse() {
        return super.canUse() && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        setDuration(-1);
        combatUser.setGlobalCooldown((int) SiliaUltInfo.READY_DURATION);
        combatUser.getWeapon().setVisible(false);
        if (!combatUser.getSkill(SiliaA3Info.getInstance()).isDurationFinished())
            combatUser.getSkill(SiliaA3Info.getInstance()).onCancelled();

        float yaw = combatUser.getEntity().getLocation().getYaw();
        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            Location loc = combatUser.getEntity().getLocation().add(0, 1, 0);
            playUseTickEffect(i, loc, yaw);

            return true;
        }, isCancelled -> {
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
     * @param i        인덱스
     * @param location 사용 위치
     * @param locYaw   원본 Yaw 값
     */
    private void playUseTickEffect(long i, Location location, float locYaw) {
        for (int j = 0; j < 6; j++) {
            long index = i * 6 + j;
            double forward = -1;
            float yaw;

            if (i > 11) {
                long subIndex = (index - 12 * 6);
                forward += 0.0025 * subIndex;
                yaw = index * 1.7F - subIndex * 4;
                location.setPitch(index * 4 + subIndex);
            } else {
                yaw = index * 1.7F;
                location.setPitch(index * 4);
            }
            location.setYaw(locYaw + yaw);

            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, LocationUtil.getLocationFromOffset(location, 0, 0, forward),
                    2, 0.15, 0.15, 0.15, 255, 255, 255);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, LocationUtil.getLocationFromOffset(location, 0, 0, forward - 0.4),
                    2, 0.15, 0.15, 0.15, 255, 255, 255);
            ParticleUtil.play(Particle.CRIT, LocationUtil.getLocationFromOffset(location, 0, 0, forward - 0.8), 2,
                    0.08, 0.08, 0.08, 0.08);
        }
    }

    /**
     * 시전 완료 시 실행할 작업.
     */
    private void onReady() {
        isEnabled = true;

        setDuration();
        ((SiliaWeapon) combatUser.getWeapon()).setStrike(true);
        combatUser.getWeapon().setVisible(true);
        combatUser.getWeapon().setGlowing(true);
        combatUser.getWeapon().displayDurability(SiliaWeaponInfo.RESOURCE.EXTENDED);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, SiliaUltInfo.SPEED);
        combatUser.getSkill(SiliaA1Info.getInstance()).setCooldown(0);

        SoundUtil.playNamedSound(NamedSound.COMBAT_SILIA_ULT_USE_READY, combatUser.getEntity().getLocation());

        TaskUtil.addTask(this, new IntervalTask(i -> !isDurationFinished(), isCancelled2 -> {
            isEnabled = false;
            ((SiliaWeapon) combatUser.getWeapon()).setStrike(false);
            combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
        }, 1));
    }
}
