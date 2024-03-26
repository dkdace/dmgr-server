package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.info.TraitInfo;
import lombok.Getter;

public final class SiliaT2Info extends TraitInfo {
    /** 피해량 */
    public static final int DAMAGE = 350;
    /** 사거리 */
    public static final double DISTANCE = 3.5;
    @Getter
    private static final SiliaT2Info instance = new SiliaT2Info();

    public SiliaT2Info() {
        super(2, "일격");
    }
}
