package com.dace.dmgr.combat.combatant.silia;

import com.dace.dmgr.combat.ability.skill.WallClimbSkill;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import lombok.NonNull;

public final class SiliaP2 extends WallClimbSkill {
    public SiliaP2(@NonNull CombatUser combatUser, @NonNull SiliaP2Info skillInfo) {
        super(combatUser, skillInfo, SiliaP2Info.USE_COUNT);
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    protected double getSpeed() {
        return SiliaP2Info.PUSH;
    }

    @Override
    protected void onWallClimbStart() {
        combatUser.getAbilityManager().getWeapon().setVisible(false);
    }

    @Override
    protected void onWallClimbTick() {
        if (combatUser.getAbilityManager().getAbility(SiliaA3Info.getInstance()).isDurationFinished())
            SiliaP2Info.Effects.USE.play(combatUser.getLocation());
        else
            SiliaP2Info.Effects.USE_A3.play(combatUser.getLocation());
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        combatUser.getAbilityManager().getWeapon().setVisible(true);
    }
}
