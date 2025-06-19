package com.dace.dmgr.combat.combatant.metar;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.Modifier;
import com.dace.dmgr.combat.entity.module.statuseffect.Silence;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;

public final class MetarP1 extends AbstractSkill {
    /** 중기갑 */
    private double heavyArmor = MetarP1Info.MAX;
    /** 수정자 */
    private final Modifier modifier = new Modifier(MetarP1Info.KNOCKBACK_RESISTANCE_INCREMENT * heavyArmor);

    public MetarP1(@NonNull CombatUser combatUser) {
        super(combatUser, MetarP1Info.getInstance(), Timespan.ZERO, Timespan.MAX);
        addOnReset(() -> heavyArmor = MetarP1Info.MAX);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.PERIODIC_1};
    }

    @Override
    @NonNull
    public String getActionBarString() {
        return ActionBarStringUtil.getProgressBar(skillInfo.toString(), (int) heavyArmor, MetarP1Info.MAX);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.getKnockbackModule().addModifier(modifier);

        addActionTask(new IntervalTask(i -> !combatUser.getStatusEffectModule().has(Silence.class), this::forceCancel, 1));
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
        combatUser.getKnockbackModule().removeModifier(modifier);
    }

    /**
     * 중기갑 수치를 증가시킨다.
     *
     * @param amount 증가량
     */
    void addValue(double amount) {
        heavyArmor = Math.min(MetarP1Info.MAX, Math.max(0, heavyArmor + amount));
        modifier.setIncrement(MetarP1Info.KNOCKBACK_RESISTANCE_INCREMENT * heavyArmor);
    }
}
