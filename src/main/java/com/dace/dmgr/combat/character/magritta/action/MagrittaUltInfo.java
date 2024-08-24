package com.dace.dmgr.combat.character.magritta.action;

import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import lombok.Getter;

public final class MagrittaUltInfo extends UltimateSkillInfo<MagrittaUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 11000;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.6 * 20);
    /** 사용 중 이동속도 감소량 */
    public static final int USE_SLOW = 40;
    /** 지속시간 (tick) */
    public static final long DURATION = (long) 3 * 20;

    /** 궁극기 처치 점수 */
    public static final int KILL_SCORE = 20;
    @Getter
    private static final MagrittaUltInfo instance = new MagrittaUltInfo();

    private MagrittaUltInfo() {
        super(MagrittaUlt.class, "초토화");
    }
}
