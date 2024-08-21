package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import lombok.Getter;

public final class VellionP2Info extends PassiveSkillInfo<VellionP2> {
    /** 치유 피해량 비율 */
    public static final double HEAL_DAMAGE_RATIO = 0.2;
    @Getter
    private static final VellionP2Info instance = new VellionP2Info();

    private VellionP2Info() {
        super(VellionP2.class, "마력 흡수");
    }
}
