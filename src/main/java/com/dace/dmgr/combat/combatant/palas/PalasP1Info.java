package com.dace.dmgr.combat.combatant.palas;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import lombok.Getter;

public final class PalasP1Info extends PassiveSkillInfo<PalasP1> {
    /** 치유량 배수 */
    public static final int HEAL_MULTIPLIER = 2;
    @Getter
    private static final PalasP1Info instance = new PalasP1Info();

    private PalasP1Info() {
        super(PalasP1.class, "응급 치료",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("치명상인 아군을 치유하면 <:HEAL_INCREASE:치유량>이 증가합니다.")
                        .addValueInfo(TextIcon.HEAL_INCREASE, "×" + HEAL_MULTIPLIER)
                        .build()));
    }
}
