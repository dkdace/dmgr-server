package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import lombok.Getter;

public final class VellionUltInfo extends UltimateSkillInfo<VellionUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 9000;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = 1 * 20;
    /** 효과 범위 (단위: 블록) */
    public static final double RADIUS = 8;
    /** 이동 속도 감소량 */
    public static final int SLOW = 30;
    /** 지속시간 (tick) */
    public static final long DURATION = (long) (2.5 * 20);
    /** 피해량 비율 */
    public static final double DAMAGE_RATIO = 0.5;
    /** 기절 시간 (tick) */
    public static final long STUN_DURATION = 1 * 20;

    /** 피해 점수 */
    public static final int DAMAGE_SCORE = 15;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 25;
    @Getter
    private static final VellionUltInfo instance = new VellionUltInfo();

    private VellionUltInfo() {
        super(VellionUlt.class, "나락의 결계");
    }
}
