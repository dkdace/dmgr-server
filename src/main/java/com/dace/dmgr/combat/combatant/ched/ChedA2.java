package com.dace.dmgr.combat.combatant.ched;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.function.LongConsumer;

public final class ChedA2 extends ActiveSkill {
    public ChedA2(@NonNull CombatUser combatUser) {
        super(combatUser, ChedA2Info.getInstance(), ChedA2Info.COOLDOWN, Timespan.MAX, 1);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_2, ActionKey.SPACE};
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && combatUser.getActionManager().getSkill(ChedP1Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setCooldown();

        Location location = combatUser.getLocation();
        location.setPitch(0);

        ChedA2Info.Sounds.USE.play(location);
        ChedA2Info.Particles.USE.play(location.clone().add(0, 0.5, 0));

        addActionTask(new IntervalTask(i -> {
            Location loc = combatUser.getLocation();

            if (location.distance(loc) > 0) {
                location.setY(loc.getY());

                Vector vec = (location.distance(loc) == 0) ? location.getDirection() : LocationUtil.getDirection(location, loc);
                vec.multiply(ChedA2Info.PUSH_SIDE);
                vec.setY(ChedA2Info.PUSH_UP);

                combatUser.getMoveModule().push(vec, true);

                return false;
            }

            return true;
        }, isCancelled -> {
            if (isCancelled)
                return;

            Vector vec = location.getDirection().multiply(ChedA2Info.PUSH_SIDE);
            vec.setY(ChedA2Info.PUSH_UP);

            combatUser.getMoveModule().push(vec, true);
        }, 1, 2));

        addActionTask(new IntervalTask((LongConsumer) i -> ChedA2Info.Particles.USE_TICK.play(combatUser.getLocation()), 1, 10));
    }

    @Override
    public boolean isCancellable() {
        return false;
    }
}
