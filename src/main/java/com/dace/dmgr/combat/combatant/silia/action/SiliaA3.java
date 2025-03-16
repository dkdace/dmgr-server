package com.dace.dmgr.combat.combatant.silia.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ChargeableSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class SiliaA3 extends ChargeableSkill {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(SiliaA3Info.SPEED);

    public SiliaA3(@NonNull CombatUser combatUser) {
        super(combatUser, SiliaA3Info.getInstance(), SiliaA3Info.COOLDOWN, SiliaA3Info.MAX_DURATION.toSeconds(), 2);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_3};
    }

    @Override
    @NonNull
    public String getActionBarString() {
        String text = ActionBarStringUtil.getDurationBar(this, Timespan.ofSeconds(getStateValue()), Timespan.ofSeconds(maxStateValue));
        if (!isDurationFinished())
            text += ActionBarStringUtil.getKeyInfo(this, "해제");

        return text;
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
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && combatUser.getSkill(SiliaUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        if (isDurationFinished()) {
            setDuration();
            combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);

            SiliaA3Info.SOUND.USE.play(combatUser.getLocation());

            double health = combatUser.getDamageModule().getHealth();
            addActionTask(new IntervalTask(i -> {
                if (getStateValue() <= 0 || health - combatUser.getDamageModule().getHealth()
                        >= combatUser.getDamageModule().getMaxHealth() * SiliaA3Info.CANCEL_DAMAGE_RATIO)
                    return false;

                combatUser.getEntity().setFallDistance(0);

                if (i >= SiliaA3Info.ACTIVATE_DURATION.toTicks() && !((SiliaWeapon) combatUser.getWeapon()).isStrike()) {
                    ((SiliaWeapon) combatUser.getWeapon()).setStrike(true);
                    SiliaA3Info.SOUND.ACTIVATE.play(combatUser.getEntity());
                }

                return true;
            }, () -> {
                cancel();
                if (getStateValue() > 0)
                    setCooldown(SiliaA3Info.COOLDOWN_FORCE);
            }, 1));
        } else
            cancel();
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setCooldown();
        ((SiliaWeapon) combatUser.getWeapon()).setStrike(false);
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER);

        SiliaA3Info.SOUND.DISABLE.play(combatUser.getLocation());
    }
}
