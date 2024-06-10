package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;

public final class VellionP1Info extends PassiveSkillInfo {
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
        super(1, "비행");
    }

    @Override
    @NonNull
    public VellionP1 createSkill(@NonNull CombatUser combatUser) {
        return new VellionP1(combatUser);
    }
}
