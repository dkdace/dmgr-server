package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

public final class NeaceA3Info extends ActiveSkillInfo<NeaceA3> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 2 * 20;
    /** 이동 강도 */
    public static final double PUSH = 0.9;
    /** 최대 거리 (단위: 블록) */
    public static final int MAX_DISTANCE = 30;
    /** 지속시간 (tick) */
    public static final long DURATION = 2 * 20;
    @Getter
    private static final NeaceA3Info instance = new NeaceA3Info();

    private NeaceA3Info() {
        super(NeaceA3.class, "도움의 손길");
    }
}
