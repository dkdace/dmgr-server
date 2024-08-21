package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ChargeableSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class SiliaA3 extends ChargeableSkill {
    /** 수정자 ID */
    private static final String MODIFIER_ID = "SiliaA3";

    public SiliaA3(@NonNull CombatUser combatUser) {
        super(combatUser, SiliaA3Info.getInstance(), 2);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_3};
    }

    @Override
    public long getDefaultCooldown() {
        return SiliaA3Info.COOLDOWN;
    }

    @Override
    public int getMaxStateValue() {
        return SiliaA3Info.MAX_DURATION;
    }

    @Override
    public int getStateValueDecrement() {
        return 1;
    }

    @Override
    public int getStateValueIncrement() {
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
            combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, SiliaA3Info.SPEED);

            SoundUtil.playNamedSound(NamedSound.COMBAT_SILIA_A3_USE, combatUser.getEntity().getLocation());

            int health = combatUser.getDamageModule().getHealth();
            TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
                if (getStateValue() <= 0 || health - combatUser.getDamageModule().getHealth() >=
                        combatUser.getDamageModule().getMaxHealth() * SiliaA3Info.CANCEL_DAMAGE_RATIO)
                    return false;

                combatUser.getEntity().setFallDistance(0);

                if (i >= SiliaA3Info.ACTIVATE_DURATION && !((SiliaWeapon) combatUser.getWeapon()).isStrike()) {
                    ((SiliaWeapon) combatUser.getWeapon()).setStrike(true);
                    SoundUtil.playNamedSound(NamedSound.COMBAT_SILIA_A3_ACTIVATE, combatUser.getEntity());
                }

                return true;
            }, isCancelled -> {
                onCancelled();
                if (getStateValue() > 0)
                    setCooldown(SiliaA3Info.COOLDOWN_FORCE);
            }, 1));
        } else
            onCancelled();
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        setCooldown();
        ((SiliaWeapon) combatUser.getWeapon()).setStrike(false);
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);

        SoundUtil.playNamedSound(NamedSound.COMBAT_SILIA_A3_DISABLE, combatUser.getEntity().getLocation());
    }
}
