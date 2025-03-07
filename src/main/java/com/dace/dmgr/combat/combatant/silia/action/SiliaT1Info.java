package com.dace.dmgr.combat.combatant.silia.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.TraitInfo;
import lombok.Getter;

public final class SiliaT1Info extends TraitInfo {
    /** 치명타 배수 */
    public static final int CRIT_MULTIPLIER = 2;

    /** 치명타 점수 */
    public static final int CRIT_SCORE = 3;
    @Getter
    private static final SiliaT1Info instance = new SiliaT1Info();

    private SiliaT1Info() {
        super("백어택",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("적의 뒤를 공격하면 <:DAMAGE_INCREASE:치명타>를 입힙니다.")
                        .addValueInfo(TextIcon.DAMAGE_INCREASE, "×" + CRIT_MULTIPLIER)
                        .build()
                )
        );
    }
}
