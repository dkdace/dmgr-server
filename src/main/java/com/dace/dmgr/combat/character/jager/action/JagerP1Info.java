package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import lombok.Getter;

public final class JagerP1Info extends PassiveSkillInfo<JagerP1> {
    /** 지속시간 (tick) */
    public static final long DURATION = 3 * 20;
    @Getter
    private static final JagerP1Info instance = new JagerP1Info();

    private JagerP1Info() {
        super(JagerP1.class, "사냥의 미학");
    }
}
