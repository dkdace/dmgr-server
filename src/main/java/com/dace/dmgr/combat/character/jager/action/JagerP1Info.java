package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import lombok.Getter;

public final class JagerP1Info extends PassiveSkillInfo<JagerP1> {
    /** 이동속도 증가량 */
    public static final int SPEED = 15;
    /** 감지 범위 (단위: 블록) */
    public static final double DETECT_RADIUS = 10;
    @Getter
    private static final JagerP1Info instance = new JagerP1Info();

    private JagerP1Info() {
        super(JagerP1.class, "사냥의 미학");
    }
}
