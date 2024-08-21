package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

public final class NeaceA1Info extends ActiveSkillInfo<NeaceA1> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 10 * 20;
    /** 초당 치유량 */
    public static final int HEAL_PER_SECOND = 120;
    /** 최대 치유량 */
    public static final int MAX_HEAL = 600;
    /** 최대 거리 (단위: 블록) */
    public static final int MAX_DISTANCE = 30;
    /** 지속시간 (tick) */
    public static final long DURATION = 15 * 20;
    @Getter
    private static final NeaceA1Info instance = new NeaceA1Info();

    private NeaceA1Info() {
        super(NeaceA1.class, "구원의 표식");
    }
}
