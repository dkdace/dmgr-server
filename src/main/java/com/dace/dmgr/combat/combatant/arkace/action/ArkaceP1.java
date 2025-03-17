package com.dace.dmgr.combat.combatant.arkace.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;

public final class ArkaceP1 extends AbstractSkill {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(ArkaceP1Info.SPRINT_SPEED);

    public ArkaceP1(@NonNull CombatUser combatUser) {
        super(combatUser, ArkaceP1Info.getInstance(), Timespan.ZERO, Timespan.MAX);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.PERIODIC_1};
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && !((ArkaceWeapon) combatUser.getWeapon()).getReloadModule().isReloading();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);

        ArkaceWeapon weapon = (ArkaceWeapon) combatUser.getWeapon();
        weapon.setDurability(ArkaceWeaponInfo.RESOURCE.SPRINT);

        addActionTask(new IntervalTask(i -> combatUser.getEntity().isSprinting() && !weapon.getReloadModule().isReloading(),
                this::cancel, 1));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);

        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER);
        combatUser.getWeapon().setDurability(ArkaceWeaponInfo.RESOURCE.DEFAULT);
    }
}
