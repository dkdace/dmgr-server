package com.dace.dmgr.combat.character.magritta.action;

import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import lombok.Getter;

public final class MagrittaP1Info extends PassiveSkillInfo<MagrittaP1> {
    /** 초당 치유량 */
    public static final int HEAL_PER_SECOND = 80;
    /** 감지 범위 (단위: 블록) */
    public static final double DETECT_RADIUS = 15;
    /** 지속시간 (tick) */
    public static final long DURATION = (long) (1.5 * 20);
    @Getter
    private static final MagrittaP1Info instance = new MagrittaP1Info();

    private MagrittaP1Info() {
        super(MagrittaP1.class, "방화광");
    }
}
