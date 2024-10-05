package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.StringFormUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class SiliaP2 extends AbstractSkill {
    /** 벽타기 남은 횟수 */
    private int wallRideCount = SiliaP2Info.USE_COUNT;

    public SiliaP2(@NonNull CombatUser combatUser) {
        super(combatUser, SiliaP2Info.getInstance());
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
     * @return 활성화 조건
     */
    private boolean canActivate() {
        if (wallRideCount <= 0)
            return false;

        Location top = combatUser.getEntity().getEyeLocation().add(0, 0.5, 0);
        if (!LocationUtil.isNonSolid(top))
            return false;

        Location loc = combatUser.getEntity().getEyeLocation().subtract(0, 0.1, 0);
        loc.setPitch(0);
        loc.add(loc.getDirection().multiply(0.75));

        return !LocationUtil.isNonSolid(loc);
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.getWeapon().setVisible(false);

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            if (!canActivate())
                return false;

            combatUser.getMoveModule().push(new Vector(0, SiliaP2Info.PUSH, 0), true);
            combatUser.getUser().sendTitle("", StringFormUtil.getProgressBar(--wallRideCount, 10, ChatColor.WHITE), 0, 10, 5);

            if (combatUser.getSkill(SiliaA3Info.getInstance()).isDurationFinished())
                SoundUtil.playNamedSound(NamedSound.COMBAT_SILIA_P2_USE, combatUser.getEntity().getLocation());
            else
                SoundUtil.playNamedSound(NamedSound.COMBAT_SILIA_P2_USE, combatUser.getEntity().getLocation(), 0.1, 0.25);

            return true;
        }, isCancelled -> {
            onCancelled();

            wallRideCount--;
            Location loc = combatUser.getEntity().getLocation();
            loc.setPitch(-65);
            combatUser.getMoveModule().push(loc.getDirection().multiply(SiliaP2Info.PUSH * 1.2), true);
        }, 3, 10));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        setDuration(0);
        combatUser.getWeapon().setVisible(true);

        TaskUtil.addTask(this, new IntervalTask(i -> !combatUser.getEntity().isOnGround(),
                isCancelled2 -> wallRideCount = SiliaP2Info.USE_COUNT, 1));
    }
}
