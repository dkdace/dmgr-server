package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.combat.action.info.TraitInfo;
import lombok.Getter;

public final class QuakerT1Info extends TraitInfo {
    /** 상태 효과 저항 */
    public static final int STATUS_EFFECT_RESISTANCE = 35;
    @Getter
    private static final QuakerT1Info instance = new QuakerT1Info();

    private QuakerT1Info() {
        super(1, "불굴");
    }
}
