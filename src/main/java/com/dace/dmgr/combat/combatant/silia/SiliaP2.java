package com.dace.dmgr.combat.combatant.silia;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.combatuser.ActionManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.StringFormUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class SiliaP2 extends AbstractSkill {
    /** 벽타기 남은 횟수 */
    private int wallRideCount = SiliaP2Info.USE_COUNT;

    public SiliaP2(@NonNull CombatUser combatUser) {
        super(combatUser, SiliaP2Info.getInstance(), Timespan.ZERO, Timespan.MAX);
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
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && canActivate();
    }

    /**
     * 스킬 활성화 조건을 확인한다.
     *
     * @return 활성화 조건
     */
    private boolean canActivate() {
        if (wallRideCount <= 0 || !LocationUtil.isNonSolid(combatUser.getEntity().getEyeLocation().add(0, 0.5, 0)))
            return false;

        Location loc = combatUser.getEntity().getEyeLocation().subtract(0, 0.1, 0);
        loc.setPitch(0);
        loc.add(loc.getDirection().multiply(0.75));

        return !LocationUtil.isNonSolid(loc);
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();

        combatUser.addYawAndPitch(0, 0);

        ActionManager actionManager = combatUser.getActionManager();
        actionManager.getWeapon().setVisible(false);

        double distance = combatUser.getEntity().getEyeLocation().distance(combatUser.getEntity().getTargetBlock(null, 1).getLocation());
        if (distance < 1)
            combatUser.getMoveModule().teleport(LocationUtil.getLocationFromOffset(combatUser.getLocation(), 0, 0, -1 + distance));

        addActionTask(new IntervalTask(i -> {
            if (combatUser.getMoveModule().isKnockbacked() || !canActivate())
                return false;

            combatUser.getMoveModule().push(new Vector(0, SiliaP2Info.PUSH, 0), true);
            combatUser.getEntity().setFallDistance(0);
            combatUser.getUser().sendTitle("", StringFormUtil.getProgressBar(--wallRideCount, 10, ChatColor.WHITE), Timespan.ZERO,
                    Timespan.ofTicks(10), Timespan.ofTicks(5));

            if (actionManager.getSkill(SiliaA3Info.getInstance()).isDurationFinished())
                SiliaP2Info.Sounds.USE.play(combatUser.getLocation(), 1, 0);
            else
                SiliaP2Info.Sounds.USE.play(combatUser.getLocation(), 0, 1);

            return true;
        }, isCancelled -> {
            cancel();

            wallRideCount--;

            Location loc = combatUser.getLocation();
            loc.setPitch(-65);
            combatUser.getMoveModule().push(loc.getDirection().multiply(SiliaP2Info.PUSH), true);
        }, 3, 10));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
        combatUser.getActionManager().getWeapon().setVisible(true);

        addTask(new IntervalTask(i -> !combatUser.getEntity().isOnGround(), () -> wallRideCount = SiliaP2Info.USE_COUNT, 1));
    }
}
