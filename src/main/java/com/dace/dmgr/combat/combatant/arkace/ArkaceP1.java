package com.dace.dmgr.combat.combatant.arkace;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.PassiveSkill;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.Modifier;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;

public final class ArkaceP1 extends PassiveSkill {
    /** 수정자 */
    private static final Modifier MODIFIER = new Modifier(ArkaceP1Info.SPRINT_SPEED);

    public ArkaceP1(@NonNull CombatUser combatUser, @NonNull ArkaceP1Info skillInfo) {
        super(combatUser, skillInfo, Timespan.ZERO, Timespan.MAX);
    }

    @Override
    protected boolean canUse() {
        return super.canUse() && isDurationFinished() && !((ArkaceWeapon) combatUser.getAbilityManager().getWeapon()).getReloadModule().isReloading();
    }

    @Override
    protected void onUse() {
        setDuration();
        combatUser.getMoveModule().addModifier(MODIFIER);

        ArkaceWeapon weapon = combatUser.getAbilityManager().getWeapon();
        weapon.setDurability(ArkaceWeaponInfo.Resource.SPRINT);

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

        combatUser.getMoveModule().removeModifier(MODIFIER);
        combatUser.getAbilityManager().getWeapon().setDurability(ArkaceWeaponInfo.Resource.DEFAULT);
    }

    /**
     * 매 틱마다 실행할 작업.
     */
    void onTick() {
        if (combatUser.getEntity().isSprinting())
            use(ActionKey.SYSTEM);
    }
}
