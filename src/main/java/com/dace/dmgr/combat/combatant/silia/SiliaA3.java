package com.dace.dmgr.combat.combatant.silia;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionBarDisplay;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.ChargeableSkill;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.Modifier;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;

public final class SiliaA3 extends ChargeableSkill {
    /** 수정자 */
    private static final Modifier MODIFIER = new Modifier(SiliaA3Info.SPEED);
    /** 누적 피해 */
    private double damageSum = 0;

    public SiliaA3(@NonNull CombatUser combatUser, @NonNull SiliaA3Info skillInfo) {
        super(combatUser, skillInfo, SiliaA3Info.COOLDOWN, SiliaA3Info.MAX_DURATION.toSeconds());
    }

    @Override
    @NonNull
    public ActionBarDisplay getActionBarDisplay() {
        ActionBarDisplay.Builder builder = ActionBarDisplay.builder(this).title()
                .durationBar(Timespan.ofSeconds(getStateValue()), Timespan.ofSeconds(maxStateValue));

        if (isDurationFinished())
            return builder.build();
        else
            return builder.keyInfo("해제").build();
    }

    @Override
    public double getStateValueDecrement() {
        return 1;
    }

    @Override
    public double getStateValueIncrement() {
        return 1;
    }

    @Override
    protected boolean canUse() {
        return super.canUse() && combatUser.getAbilityManager().getAbility(SiliaUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    @NonNull
    public ActionKey.Slot getSlot() {
        return ActionKey.Slot.SLOT_3;
    }

    @Override
    public void onSlot() {
        if (!isDurationFinished()) {
            cancel();
            return;
        }

        setDuration();
        combatUser.getMoveModule().addModifier(MODIFIER);

        SiliaA3Info.Effects.ON.play(combatUser.getLocation());

        addActionTask(new IntervalTask(i -> {
            if (getStateValue() <= 0)
                return false;

            combatUser.getEntity().setFallDistance(0);
            return true;
        }, this::cancel, 1));

        addActionTask(new DelayTask(() -> {
            combatUser.getAbilityManager().getAbility(SiliaT2Info.getInstance()).setStrike(true);
            SiliaA3Info.Effects.STRIKE_ACTIVATE.play(combatUser.getEntity());
        }, SiliaA3Info.ACTIVATE_DURATION.toTicks()));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);

        combatUser.getMoveModule().removeModifier(MODIFIER);
        damageSum = 0;

        combatUser.getAbilityManager().getAbility(SiliaT2Info.getInstance()).setStrike(false);

        SiliaA3Info.Effects.OFF.play(combatUser.getLocation());
    }

    /**
     * 피해를 입었을 때 실행될 작업.
     *
     * @param damage 피해량
     */
    void onDamage(double damage) {
        if (isDurationFinished())
            return;

        damageSum += damage;
        if (damageSum >= combatUser.getDamageModule().getMaxHealth() * SiliaA3Info.CANCEL_DAMAGE_RATIO) {
            cancel();
            setCooldown(SiliaA3Info.COOLDOWN_FORCE);
        }
    }
}
