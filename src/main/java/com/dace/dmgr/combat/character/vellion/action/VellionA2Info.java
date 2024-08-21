package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

public final class VellionA2Info extends ActiveSkillInfo<VellionA2> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 5 * 20;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.8 * 20);
    /** 시전 중 이동속도 감소량 */
    public static final int READY_SLOW = 70;
    /** 초당 피해량 */
    public static final int DAMAGE_PER_SECOND = 60;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 8;
    /** 최대 거리 (단위: 블록) */
    public static final int MAX_DISTANCE = 20;
    /** 방어력 감소량 */
    public static final int DEFENSE_DECREMENT = 25;
    /** 대상 위치 통과 불가 시 초기화 제한 시간 (tick) */
    public static final long BLOCK_RESET_DELAY = 2 * 20;

    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 20;
    @Getter
    private static final VellionA2Info instance = new VellionA2Info();

    private VellionA2Info() {
        super(VellionA2.class, "저주 귀속");
    }
}
