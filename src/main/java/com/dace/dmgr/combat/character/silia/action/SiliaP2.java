package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.StringFormUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

public final class SiliaP2 extends AbstractSkill {
    /** 벽타기 남은 횟수 */
    private int wallRideCount;

    public SiliaP2(@NonNull CombatUser combatUser) {
        super(1, combatUser, SiliaP1Info.getInstance());
        wallRideCount = SiliaP2Info.USE_COUNT;
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
    public boolean canUse() {
        return super.canUse() && isDurationFinished() && canActivate();
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

        Location loc = combatUser.getEntity().getEyeLocation().subtract(0, 0.2, 0);
        loc.setPitch(0);
        loc.add(loc.getDirection());

        return !LocationUtil.isNonSolid(loc);
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        combatUser.setGlobalCooldown(-1);
        setDuration();
        combatUser.getWeapon().displayDurability(SiliaWeaponInfo.RESOURCE.USE);

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            if (!canActivate())
                return false;
            if (wallRideCount <= 0)
                return false;

            combatUser.push(new Vector(0, 0.45, 0), true);
            combatUser.getUser().sendTitle("", StringFormUtil.getProgressBar(--wallRideCount, 10, ChatColor.WHITE), 0, 10, 5);
            SoundUtil.play(Sound.BLOCK_STONE_STEP, combatUser.getEntity().getLocation(), 0.9, 0.55, 0.05);

            return true;
        }, isCancelled -> {
            combatUser.resetGlobalCooldown();
            setDuration(0);
            combatUser.getWeapon().displayDurability(SiliaWeaponInfo.RESOURCE.DEFAULT);
            combatUser.push(new Vector(0, 0.4, 0), true);

            TaskUtil.addTask(taskRunner, new IntervalTask(i -> !combatUser.getEntity().isOnGround(),
                    isCancelled2 -> wallRideCount = SiliaP2Info.USE_COUNT, 1));
        }, 3, 10));
    }
}
