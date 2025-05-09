package com.dace.dmgr.combat.combatant.ched;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.combatuser.ActionManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.util.StringFormUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public final class ChedP1 extends AbstractSkill {
    /** 벽타기 남은 횟수 */
    private int wallRideCount = ChedP1Info.USE_COUNT;
    /** 매달리기 남은 시간 (tick) */
    private long hangTick = ChedP1Info.HANG_DURATION.toTicks();
    /** 매달리기 활성화 여부 */
    @Getter(AccessLevel.PACKAGE)
    private boolean isHanging = false;

    public ChedP1(@NonNull CombatUser combatUser) {
        super(combatUser, ChedP1Info.getInstance(), Timespan.ZERO, Timespan.MAX);
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
    @Nullable
    public String getActionBarString() {
        return isDurationFinished() ? null : ActionBarStringUtil.getDurationBar(this, Timespan.ofTicks(hangTick), ChedP1Info.HANG_DURATION);
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
        return canActivate(combatUser.getLocation().getYaw());
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.addYawAndPitch(0, 0);

        ActionManager actionManager = combatUser.getActionManager();
        ChedWeapon weapon = (ChedWeapon) actionManager.getWeapon();
        weapon.setVisible(false);

        Location location = combatUser.getEntity().getEyeLocation();
        double distance = location.distance(combatUser.getEntity().getTargetBlock(null, 1).getLocation());
        if (distance < 1)
            combatUser.getMoveModule().teleport(LocationUtil.getLocationFromOffset(combatUser.getLocation(), 0, 0, -1 + distance));

        float yaw = location.getYaw();

        addActionTask(new IntervalTask(i -> {
            if (combatUser.getMoveModule().isKnockbacked())
                return false;

            if (combatUser.getEntity().isSneaking() && hangTick > 0)
                return canActivate(yaw);
            if (!canActivate())
                return false;

            if (isHanging)
                setHanging(false);

            weapon.setCanShoot(false);

            actionManager.getSkill(ChedA3Info.getInstance()).cancel();
            actionManager.getSkill(ChedUltInfo.getInstance()).cancel();

            combatUser.getMoveModule().push(new Vector(0, ChedP1Info.PUSH, 0), true);
            combatUser.getEntity().setFallDistance(0);

            combatUser.getUser().sendTitle("", StringFormUtil.getProgressBar(--wallRideCount, 10, ChatColor.WHITE), Timespan.ZERO,
                    Timespan.ofTicks(10), Timespan.ofTicks(5));
            ChedP1Info.Sounds.USE.play(combatUser.getLocation());

            return true;
        }, () -> {
            cancel();

            wallRideCount--;

            Location loc = combatUser.getLocation();
            loc.setPitch(-65);
            combatUser.getMoveModule().push(loc.getDirection().multiply(ChedP1Info.PUSH), true);
        }, 3));

        runHangTask();
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);

        addTask(new IntervalTask(i -> !combatUser.getEntity().isOnGround(), () -> {
            wallRideCount = ChedP1Info.USE_COUNT;
            hangTick = ChedP1Info.HANG_DURATION.toTicks();
        }, 1));

        if (isHanging) {
            setHanging(false);

            ChedP1Info.Sounds.DISABLE_HANG.play(combatUser.getLocation());
            ChedP1Info.Particles.USE_HANG.play(combatUser.getLocation());
        } else
            combatUser.getActionManager().getWeapon().setVisible(true);
    }

    /**
     * 매달리기 사용 태스크를 실행한다.
     */
    private void runHangTask() {
        addActionTask(new IntervalTask(i -> {
            if (hangTick <= 0)
                return false;
            if (!combatUser.getEntity().isSneaking())
                return true;

            if (!isHanging) {
                setHanging(true);

                ChedP1Info.Sounds.USE_HANG.play(combatUser.getLocation());
                ChedP1Info.Particles.USE_HANG.play(combatUser.getLocation());
            }

            hangTick--;

            combatUser.getMoveModule().push(new Vector(), true);

            playHangTickEffect();

            return true;
        }, 1));
    }

    /**
     * 매달리기 사용 중 효과를 재생한다.
     */
    private void playHangTickEffect() {
        Location loc = combatUser.getLocation();
        loc.setYaw(0);
        loc.setPitch(0);
        Vector vector = VectorUtil.getRollAxis(loc).multiply(0.65);
        Vector axis = VectorUtil.getYawAxis(loc);

        for (int i = 0; i < 7; i++) {
            int angle = 360 / 7 * i;
            Vector vec = VectorUtil.getRotatedVector(vector, axis, angle);

            ChedP1Info.Particles.TICK_HANG.play(loc.clone().add(vec));
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
            combatUser.getActionManager().getWeapon().setVisible(isHanging);
    }
}
