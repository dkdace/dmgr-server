package com.dace.dmgr.combat.character.inferno.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.TraitInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class InfernoT1Info extends TraitInfo {
    /** 회복량 감소 */
    public static final int HEAL_DECREMENT = 50;
    @Getter
    private static final InfernoT1Info instance = new InfernoT1Info();

    private InfernoT1Info() {
        super("융해",
                "",
                "§f▍ 인페르노가 입히는 화염 피해는 적의 §a" + TextIcon.HEAL_DECREASE + " 회복량",
                "§f▍ 을 감소시킵니다.",
                "",
                MessageFormat.format("§a{0}§f {1}%", TextIcon.HEAL_DECREASE, HEAL_DECREMENT));
    }
}
