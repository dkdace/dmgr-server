package com.dace.dmgr.combat.character.magritta.action;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

public final class MagrittaA2Info extends ActiveSkillInfo<MagrittaA2> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 10 * 20;
    /** 이동속도 증가량 */
    public static final int SPEED = 60;
    /** 지속 시간 (tick) */
    public static final long DURATION = 1 * 20;
    @Getter
    private static final MagrittaA2Info instance = new MagrittaA2Info();

    private MagrittaA2Info() {
        super(MagrittaA2.class, "불꽃의 그림자");
    }
}
