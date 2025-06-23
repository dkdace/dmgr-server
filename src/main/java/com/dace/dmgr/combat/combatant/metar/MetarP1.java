package com.dace.dmgr.combat.combatant.metar;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionBarDisplay;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.Modifier;
import com.dace.dmgr.combat.entity.module.statuseffect.Silence;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;

import java.util.EnumSet;
import java.util.Set;

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
    public Set<@NonNull ActionKey> getDefaultActionKeys() {
        return EnumSet.of(ActionKey.PERIODIC_1);
    }

    @Override
    @NonNull
    public ActionBarDisplay getActionBarDisplay() {
        return ActionBarDisplay.builder(this).title().progressBar((int) heavyArmor, MetarP1Info.MAX).build();
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
    private void addValue(double amount) {
        heavyArmor = Math.min(MetarP1Info.MAX, Math.max(0, heavyArmor + amount));
        modifier.setIncrement(MetarP1Info.KNOCKBACK_RESISTANCE_INCREMENT * heavyArmor);
    }

    /**
     * 매 틱마다 실행할 작업.
     */
    void onTick() {
        addValue(MetarP1Info.RECOVER_PER_SECOND / 20.0);
        combatUser.getActionManager().useAction(ActionKey.PERIODIC_1);
    }

    /**
     * 강제로 밀쳐졌을 때 실행될 작업.
     *
     * @param speed 속력
     */
    void onKnockbacked(double speed) {
        addValue(-speed * MetarP1Info.DECREASE_MULTIPLIER);
    }
}
