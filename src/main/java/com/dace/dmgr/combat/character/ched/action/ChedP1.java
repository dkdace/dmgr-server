package com.dace.dmgr.combat.character.ched.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class ChedP1 extends AbstractSkill {
    /** 벽타기 남은 횟수 */
    private int wallRideCount = ChedP1Info.USE_COUNT;
    /** 매달리기 남은 시간 (tick) */
    @Getter
    private long hangTick = ChedP1Info.HANG_DURATION;
    /** 매달리기 활성화 여부 */
    @Getter
    private boolean isHanging = false;

    public ChedP1(@NonNull CombatUser combatUser) {
        super(combatUser);
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.LEFT_CLICK};
    }

    @Override
    public long getDefaultCooldown() {
        return 0;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && canActivate();
    }

    /**
     * 스킬 활성화 조건을 확인한다.
     *
     * @param yaw 원본 Yaw 값
     * @return 활성화 조건
     */
    private boolean canActivate(float yaw) {
        if (wallRideCount <= 0 || !LocationUtil.isNonSolid(combatUser.getEntity().getEyeLocation().add(0, 0.5, 0)))
            return false;

        Location loc = combatUser.getEntity().getEyeLocation().subtract(0, 0.1, 0);
        loc.setYaw(yaw);
        loc.setPitch(0);
        loc.add(loc.getDirection().multiply(0.75));

        return !LocationUtil.isNonSolid(loc);
    }

    /**
     * 스킬 활성화 조건을 확인한다.
     *
     * @return 활성화 조건
     */
    private boolean canActivate() {
        return canActivate(combatUser.getEntity().getLocation().getYaw());
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.getWeapon().setVisible(false);

        float yaw = combatUser.getEntity().getEyeLocation().getYaw();

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            if (combatUser.getKnockbackModule().isKnockbacked())
                return false;

            if (combatUser.getEntity().isSneaking() && hangTick > 0)
                return canActivate(yaw);
            if (!canActivate())
                return false;

            if (isHanging)
                setHanging(false);

            ((ChedWeapon) combatUser.getWeapon()).setCanShoot(false);
            combatUser.getMoveModule().push(new Vector(0, ChedP1Info.PUSH, 0), true);
            combatUser.getEntity().setFallDistance(0);
            combatUser.getUser().sendTitle("", StringFormUtil.getProgressBar(--wallRideCount, 10, ChatColor.WHITE), 0, 10, 5);

            SoundUtil.playNamedSound(NamedSound.COMBAT_CHED_P1_USE, combatUser.getEntity().getLocation());

            return true;
        }, isCancelled -> {
            onCancelled();

            wallRideCount--;
            Location loc = combatUser.getEntity().getLocation();
            loc.setPitch(-65);
            combatUser.getMoveModule().push(loc.getDirection().multiply(ChedP1Info.PUSH * 1.2), true);
        }, 3));

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            if (hangTick <= 0)
                return false;
            if (!combatUser.getEntity().isSneaking())
                return true;

            if (!isHanging) {
                setHanging(true);

                SoundUtil.playNamedSound(NamedSound.COMBAT_CHED_P1_USE_HANG, combatUser.getEntity().getLocation());
                ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, combatUser.getEntity().getLocation(), 40,
                        0.65, 0, 0.65, 186, 55, 30);
            }

            hangTick--;
            combatUser.getMoveModule().push(new Vector(), true);

            playTickEffect();

            return true;
        }, 1));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        setDuration(0);

        TaskUtil.addTask(this, new IntervalTask(i -> !combatUser.getEntity().isOnGround(), isCancelled -> {
            wallRideCount = ChedP1Info.USE_COUNT;
            hangTick = ChedP1Info.HANG_DURATION;
        }, 1));

        if (isHanging) {
            setHanging(false);

            SoundUtil.playNamedSound(NamedSound.COMBAT_CHED_P1_DISABLE_HANG, combatUser.getEntity().getLocation());
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, combatUser.getEntity().getLocation(), 40,
                    0.65, 0, 0.65, 186, 55, 30);
        } else
            combatUser.getWeapon().setVisible(true);
    }

    /**
     * 사용 중 효과를 재생한다.
     */
    private void playTickEffect() {
        Location loc = combatUser.getEntity().getLocation();
        loc.setYaw(0);
        loc.setPitch(0);
        Vector vector = VectorUtil.getRollAxis(loc).multiply(0.65);
        Vector axis = VectorUtil.getYawAxis(loc);

        for (int j = 0; j < 7; j++) {
            int angle = 360 / 7 * j;
            Vector vec = VectorUtil.getRotatedVector(vector, axis, angle);

            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc.clone().add(vec),
                    1, 0.24, 0, 0.24, 186, 55, 30);
        }
    }

    /**
     * 매달리기 상태를 설정한다.
     *
     * @param isEnabled 활성화 여부
     */
    private void setHanging(boolean isEnabled) {
        isHanging = isEnabled;
        combatUser.getEntity().setGravity(!isHanging);
        if (!isDurationFinished())
            combatUser.getWeapon().setVisible(isHanging);
    }
}
