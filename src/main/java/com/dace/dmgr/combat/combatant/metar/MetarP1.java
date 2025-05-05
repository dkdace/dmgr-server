package com.dace.dmgr.combat.combatant.metar;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public final class MetarP1 extends AbstractSkill {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(100);

    public MetarP1(@NonNull CombatUser combatUser) {
        super(combatUser, MetarP1Info.getInstance(), MetarP1Info.COOLDOWN, Timespan.MAX);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.PERIODIC_1};
    }

    @Override
    @Nullable
    public String getActionBarString() {
        if (!isCooldownFinished())
            return ActionBarStringUtil.getCooldownBar(this);
        else if (!isDurationFinished())
            return skillInfo + " §a활성화";

        return null;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.getMoveModule().getResistanceStatus().addModifier(MODIFIER);

        MetarP1Info.Sounds.USE.play(combatUser.getLocation());

        addActionTask(new IntervalTask(i -> combatUser.getEntity().isSneaking(), this::forceCancel, 1));
    }

    @Override
    public boolean isCancellable() {
        return combatUser.isDead();
    }

    @Override
    protected void onCancelled() {
        setCooldown();
        combatUser.getMoveModule().getResistanceStatus().removeModifier(MODIFIER);

        MetarP1Info.Sounds.DISABLE.play(combatUser.getLocation());
    }
}
