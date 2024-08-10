package com.dace.dmgr.combat.character.inferno.action;

import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import lombok.Getter;

public final class InfernoP1Info extends PassiveSkillInfo<InfernoP1> {
    /** 방어력 증가량 */
    public static final int DEFENSE_INCREMENT = 40;
    /** 지속시간 (tick) */
    public static final long DURATION = (long) (1.5 * 20);
    @Getter
    private static final InfernoP1Info instance = new InfernoP1Info();

    private InfernoP1Info() {
        super(InfernoP1.class, "불꽃의 용기");
    }
}
