package com.dace.dmgr.combat.character.inferno.action;

import com.dace.dmgr.combat.action.info.TraitInfo;
import lombok.Getter;

public final class InfernoT1Info extends TraitInfo {
    /** 회복량 감소 */
    public static final int HEAL_DECREMENT = 50;
    @Getter
    private static final InfernoT1Info instance = new InfernoT1Info();

    private InfernoT1Info() {
        super("융해");
    }
}
