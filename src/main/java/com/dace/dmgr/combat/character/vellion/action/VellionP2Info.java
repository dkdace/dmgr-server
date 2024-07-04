package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;

public final class VellionP2Info extends PassiveSkillInfo {
    /** 치유 피해량 비율 */
    public static final double HEAL_DAMAGE_RATIO = 0.2;
    @Getter
    private static final VellionP2Info instance = new VellionP2Info();

    private VellionP2Info() {
        super(2, "마력 흡수");
    }

    @Override
    @NonNull
    public VellionP2 createSkill(@NonNull CombatUser combatUser) {
        return new VellionP2(combatUser);
    }
}
