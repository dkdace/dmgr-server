package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.info.TraitInfo;
import lombok.Getter;

public final class JagerT1Info extends TraitInfo {
    /** 지속시간 (tick) */
    public static final long DURATION = 2 * 20;
    /** 달리기 불가능 수치 */
    public static final int NO_SPRINT = 60;
    /** 점프 불가능 수치 */
    public static final int NO_JUMP = 80;
    /** 최대치 */
    public static final int MAX = 100;
    @Getter
    private static final JagerT1Info instance = new JagerT1Info();

    private JagerT1Info() {
        super("빙결");
    }
}
