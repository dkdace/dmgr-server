package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.info.TraitInfo;
import lombok.Getter;

public final class SiliaT1Info extends TraitInfo {
    /** 치명타 배수 */
    public static final int CRIT_MULTIPLIER = 2;

    /** 치명타 점수 */
    public static final int CRIT_SCORE = 5;
    @Getter
    private static final SiliaT1Info instance = new SiliaT1Info();

    private SiliaT1Info() {
        super(1, "백어택");
    }
}
