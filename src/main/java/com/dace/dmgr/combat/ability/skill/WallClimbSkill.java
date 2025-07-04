package com.dace.dmgr.combat.ability.skill;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.handler.LeftClickHandler;
import com.dace.dmgr.combat.ability.info.PassiveSkillInfo;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.util.StringFormUtil;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 벽타기 패시브 스킬 클래스.
 */
public abstract class WallClimbSkill extends PassiveSkill implements LeftClickHandler {
    /** 최대 벽타기 횟수 */
    private final int maxWallClimbCount;
    /** 남은 벽타기 횟수 */
    private int wallClimbCount;

    /**
     * 벽타기 패시브 스킬 인스턴스를 생성한다.
     *
     * @param combatUser        사용자 플레이어
     * @param skillInfo         패시브 스킬 정보 인스턴스
     * @param maxWallClimbCount 최대 벽타기 횟수
     */
    protected WallClimbSkill(@NonNull CombatUser combatUser, @NonNull PassiveSkillInfo<?> skillInfo, int maxWallClimbCount) {
        super(combatUser, skillInfo, Timespan.ZERO, Timespan.MAX);

        this.maxWallClimbCount = maxWallClimbCount;
        this.wallClimbCount = maxWallClimbCount;
    }

    @Override
    protected final boolean canUse() {
        return super.canUse() && isDurationFinished() && canActivate(combatUser.getLocation());
    }

    /**
     * 스킬 활성화 조건을 확인한다.
     *
     * @param location 확인할 위치
     * @return 활성화 조건
     */
    private boolean canActivate(@NonNull Location location) {
        if (wallClimbCount <= 0 || !LocationUtil.isNonSolid(combatUser.getEntity().getEyeLocation().add(0, 0.5, 0)))
            return false;

        location.setPitch(0);
        Location loc = combatUser.getEntity().getEyeLocation().subtract(0, 0.1, 0).add(location.getDirection().multiply(0.75));

        return !LocationUtil.isNonSolid(loc);
    }

    @Override
    public final void onLeftClick() {
        setDuration();

        combatUser.addYawAndPitch(0, 0);
        onWallClimbStart();

        double distance = combatUser.getEntity().getEyeLocation().distance(combatUser.getEntity().getTargetBlock(null, 1)
                .getLocation());
        if (distance < 1)
            combatUser.getMoveModule().teleport(LocationUtil.getLocationFromOffset(combatUser.getLocation(), 0, 0, -1 + distance));

        Location location = combatUser.getLocation();

        addActionTask(new IntervalTask(i -> {
            if (combatUser.getKnockbackModule().isKnockbacked())
                return false;
            if (!canWallClimb())
                return canActivate(location);
            if (!canActivate(combatUser.getLocation()))
                return false;

            combatUser.getMoveModule().push(new Vector(0, getSpeed(), 0), true);
            combatUser.getEntity().setFallDistance(0);
            combatUser.getUser().sendTitle("", StringFormUtil.getProgressBar(--wallClimbCount, 10, ChatColor.WHITE), Timespan.ZERO,
                    Timespan.ofTicks(10), Timespan.ofTicks(5));

            onWallClimbTick();
            return true;
        }, () -> {
            cancel();

            wallClimbCount--;

            Location loc = combatUser.getLocation();
            loc.setPitch(-65);
            combatUser.getMoveModule().push(loc.getDirection().multiply(getSpeed()), true);
        }, 3));
    }

    /**
     * 벽타기의 속력을 반환한다.
     *
     * @return 속력
     */
    protected abstract double getSpeed();

    /**
     * 벽타기를 시작했을 때 실행할 작업.
     */
    protected abstract void onWallClimbStart();

    /**
     * 벽타기를 진행할 수 있는지 확인한다.
     *
     * @return 벽타기 진행 가능 여부. {@code false} 반환 시 벽타기 일시정지
     */
    protected boolean canWallClimb() {
        return true;
    }

    /**
     * 벽타기 중 틱마다 실행할 작업.
     */
    protected abstract void onWallClimbTick();

    @Override
    public final boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    @MustBeInvokedByOverriders
    protected void onCancelled() {
        setDuration(Timespan.ZERO);

        addTask(new IntervalTask(i -> !combatUser.getEntity().isOnGround(), () -> {
            wallClimbCount = maxWallClimbCount;
            onLand();
        }, 1));
    }

    /**
     * 벽타기 후 바닥에 착지 시 실행할 작업.
     */
    protected void onLand() {
        // 미사용
    }
}
