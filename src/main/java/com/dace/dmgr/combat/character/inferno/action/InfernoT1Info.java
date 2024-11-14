package com.dace.dmgr.combat.character.inferno.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.TraitInfo;
import lombok.Getter;

public final class InfernoT1Info extends TraitInfo {
    /** 회복량 감소 */
    public static final int HEAL_DECREMENT = 50;
    @Getter
    private static final InfernoT1Info instance = new InfernoT1Info();

    private InfernoT1Info() {
        super("융해",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("인페르노가 입히는 화염 피해는 적의 <:HEAL_DECREASE:회복량>을 감소시킵니다.")
                        .addValueInfo(TextIcon.HEAL_DECREASE, Format.PERCENT, HEAL_DECREMENT)
                        .build()
                )
        );
    }
}
