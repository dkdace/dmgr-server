package com.dace.dmgr.combat.combatant.arkace;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionBarDisplay;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public final class ArkaceA2 extends ActiveSkill {
    public ArkaceA2(@NonNull CombatUser combatUser, @NonNull ArkaceA2Info skillInfo) {
        super(combatUser, skillInfo, ArkaceA2Info.COOLDOWN, ArkaceA2Info.DURATION);
    }

    @Override
    @Nullable
    public ActionBarDisplay getActionBarDisplay() {
        if (isDurationFinished())
            return null;

        return ActionBarDisplay.builder(this).title().durationBar().build();
    }

    @Override
    protected boolean canUse() {
        return super.canUse() && isDurationFinished();
    }

    @Override
    @NonNull
    public ActionKey.Slot getSlot() {
        return ActionKey.Slot.SLOT_3;
    }

    @Override
    public void onSlot() {
        setDuration();

        ArkaceA2Info.Effects.USE.play(combatUser.getLocation());

        long durationTicks = ArkaceA2Info.DURATION.toTicks();

        addActionTask(new IntervalTask(i -> {
            if (combatUser.getHealModule().heal(combatUser, (double) ArkaceA2Info.HEAL / durationTicks, true))
                combatUser.addScore(ArkaceA2Info.HEAL_SCORE.divideScore(durationTicks));

            ArkaceA2Info.Effects.playTick(i, combatUser.getLocation());
        }, 1, durationTicks));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
    }
}
