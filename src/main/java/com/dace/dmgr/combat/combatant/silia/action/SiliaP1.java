package com.dace.dmgr.combat.combatant.silia.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class SiliaP1 extends AbstractSkill {
    public SiliaP1(@NonNull CombatUser combatUser) {
        super(combatUser, SiliaP1Info.getInstance(), Timespan.ZERO, Timespan.MAX);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SPACE};
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && combatUser.getSkill(SiliaP2Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();

        Location location = combatUser.getLocation();
        if (combatUser.getSkill(SiliaA3Info.getInstance()).isDurationFinished())
            SiliaP1Info.SOUND.USE.play(location, 1, 0);
        else
            SiliaP1Info.SOUND.USE.play(location, 0, 1);

        addActionTask(new IntervalTask(i -> {
            Location loc = combatUser.getLocation();

            if (location.distance(loc) > 0) {
                location.setY(loc.getY());
                Vector vec = (location.distance(loc) == 0) ? new Vector(0, 0, 0) :
                        LocationUtil.getDirection(location, loc).multiply(SiliaP1Info.PUSH_SIDE);
                vec.setY(SiliaP1Info.PUSH_UP);

                combatUser.getMoveModule().push(vec, true);

                return false;
            }

            return true;
        }, isCancelled -> onCancelled(), 1, 2));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        addTask(new IntervalTask(i -> !combatUser.getEntity().isOnGround(),
                () -> setDuration(Timespan.ZERO), 1));
    }
}
