package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import lombok.Getter;

public final class NeaceP1Info extends PassiveSkillInfo<NeaceP1> {
    /** 초당 치유량 */
    public static final int HEAL_PER_SECOND = 100;
    /** 활성화 시간 (tick) */
    public static final long ACTIVATE_DURATION = (long) (2.5 * 20);
    @Getter
    private static final NeaceP1Info instance = new NeaceP1Info();

    private NeaceP1Info() {
        super(NeaceP1.class, "생명의 힘");
    }
}
