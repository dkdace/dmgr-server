package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import lombok.Getter;

public final class VellionP1Info extends PassiveSkillInfo<VellionP1> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 5 * 20;
    /** 이동속도 증가량 */
    public static final int SPEED = 20;
    /** 수직 이동 강도 */
    public static final double PUSH_UP = 0.2;
    /** 수평 이동 강도 */
    public static final double PUSH_SIDE = 0.6;
    /** 지속시간 (tick) */
    public static final int DURATION = 10 * 20;
    @Getter
    private static final VellionP1Info instance = new VellionP1Info();

    private VellionP1Info() {
        super(VellionP1.class, "비행");
    }
}
