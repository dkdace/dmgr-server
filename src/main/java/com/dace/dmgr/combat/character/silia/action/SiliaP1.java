package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class SiliaP1 extends AbstractSkill {
    public SiliaP1(@NonNull CombatUser combatUser) {
        super(1, combatUser, SiliaP1Info.getInstance());
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SPACE};
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
        return super.canUse() && isDurationFinished() && combatUser.getSkill(SiliaP2Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        Location location = combatUser.getEntity().getLocation();
        if (combatUser.getSkill(SiliaA3Info.getInstance()).isDurationFinished())
            SoundUtil.play(NamedSound.COMBAT_SILIA_P1_USE, location);
        else
            SoundUtil.play(NamedSound.COMBAT_SILIA_P1_USE, location, 0.1, 0.2);

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            Location loc = combatUser.getEntity().getLocation();

            if (location.distance(loc) > 0) {
                location.setY(loc.getY());
                Vector vec = (location.distance(loc) == 0) ? new Vector(0, 0, 0) : LocationUtil.getDirection(location, loc).multiply(0.35);
                vec.setY(0.55);

                combatUser.push(vec, true);

                return false;
            }

            return true;
        }, isCancelled -> {
            TaskUtil.addTask(SiliaP1.this, new IntervalTask(i -> !combatUser.getEntity().isOnGround(),
                    isCancelled2 -> setDuration(0), 1));
        }, 1, 2));
    }
}