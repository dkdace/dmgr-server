package com.dace.dmgr.combat.combatant.vellion.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import lombok.Getter;

public final class VellionP2Info extends PassiveSkillInfo<VellionP2> {
    /** 치유 피해량 비율 */
    public static final double HEAL_DAMAGE_RATIO = 0.2;
    @Getter
    private static final VellionP2Info instance = new VellionP2Info();

    private VellionP2Info() {
        super(VellionP2.class, "마력 흡수",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("적에게 피해를 입히면 <:HEAL:회복>합니다.")
                        .addValueInfo(TextIcon.HEAL, "피해량의 {0}%", (int) (100 * HEAL_DAMAGE_RATIO))
                        .build()));
    }
}
