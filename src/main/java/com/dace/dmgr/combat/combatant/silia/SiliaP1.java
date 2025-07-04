package com.dace.dmgr.combat.combatant.silia;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.handler.SpaceHandler;
import com.dace.dmgr.combat.ability.skill.PassiveSkill;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class SiliaP1 extends PassiveSkill implements SpaceHandler {
    public SiliaP1(@NonNull CombatUser combatUser, @NonNull SiliaP1Info skillInfo) {
        super(combatUser, skillInfo, Timespan.ZERO, Timespan.MAX);
    }

    @Override
    protected boolean canUse() {
        return super.canUse() && isDurationFinished() && combatUser.getAbilityManager().getAbility(SiliaP2Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onSpace() {
        setDuration();

        Location location = combatUser.getLocation();
        if (combatUser.getAbilityManager().getAbility(SiliaA3Info.getInstance()).isDurationFinished())
            SiliaP1Info.Effects.USE.play(location);
        else
            SiliaP1Info.Effects.USE_A3.play(location);

        addActionTask(new IntervalTask(i -> {
            Location loc = combatUser.getLocation();

            if (location.distance(loc) > 0) {
                location.setY(loc.getY());

                Vector vec = (location.distance(loc) == 0) ? new Vector(0, 0, 0) : LocationUtil.getDirection(location, loc);
                vec.multiply(SiliaP1Info.PUSH_SIDE);
                vec.setY(SiliaP1Info.PUSH_UP);

                combatUser.getMoveModule().push(vec, true);

                return false;
            }

            return true;
        }, isCancelled -> cancel(), 1, 2));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        addTask(new IntervalTask(i -> !combatUser.getEntity().isOnGround(), () -> setDuration(Timespan.ZERO), 1));
    }
}
