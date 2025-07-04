package com.dace.dmgr.combat.combatant.ched;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.handler.SpaceHandler;
import com.dace.dmgr.combat.ability.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.function.LongConsumer;

public final class ChedA2 extends ActiveSkill implements SpaceHandler {
    public ChedA2(@NonNull CombatUser combatUser, @NonNull ChedA2Info skillInfo) {
        super(combatUser, skillInfo, ChedA2Info.COOLDOWN, Timespan.MAX);
    }

    @Override
    protected boolean canUse() {
        return super.canUse() && combatUser.getAbilityManager().getAbility(ChedP1Info.getInstance()).isDurationFinished();
    }

    @Override
    @NonNull
    public ActionKey.Slot getSlot() {
        return ActionKey.Slot.SLOT_2;
    }

    @Override
    public void onSlot() {
        setCooldown();

        Location location = combatUser.getLocation();
        location.setPitch(0);

        ChedA2Info.Effects.playUse(location);

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

        addActionTask(new IntervalTask((LongConsumer) i -> ChedA2Info.Effects.USE_TICK.play(combatUser.getLocation()), 1, 10));
    }

    @Override
    public void onSpace() {
        onSlot();
    }

    @Override
    public boolean isCancellable() {
        return false;
    }
}
