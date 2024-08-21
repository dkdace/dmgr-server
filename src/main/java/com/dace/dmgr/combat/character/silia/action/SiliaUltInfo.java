package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import lombok.Getter;

public final class SiliaUltInfo extends UltimateSkillInfo<SiliaUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 8000;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = 1 * 20;
    /** 지속시간 (tick) */
    public static final long DURATION = 4 * 20;
    /** 처치 시 지속시간 증가 (tick) */
    public static final long DURATION_ADD_ON_KILL = 2 * 20;
    /** 이동속도 증가량 */
    public static final int SPEED = 30;
    /** 일격 쿨타임 (tick) */
    public static final long STRIKE_COOLDOWN = (long) (0.55 * 20);

    /** 궁극기 처치 점수 */
    public static final int KILL_SCORE = 25;
    @Getter
    private static final SiliaUltInfo instance = new SiliaUltInfo();

    private SiliaUltInfo() {
        super(SiliaUlt.class, "폭풍의 부름");
    }
}
