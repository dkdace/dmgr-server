package com.dace.dmgr.combat.character.inferno.action;

import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import lombok.Getter;

public final class InfernoUltInfo extends UltimateSkillInfo<InfernoUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 8000;
    /** 체력 */
    public static final int HEALTH = 3000;
    /** 액티브 1번 쿨타임 단축 (tick) */
    public static final int A1_COOLDOWN_DECREMENT = 3 * 20;
    /** 지속시간 (tick) */
    public static final long DURATION = 10 * 20;

    /** 궁극기 처치 점수 */
    public static final int KILL_SCORE = 20;
    @Getter
    private static final InfernoUltInfo instance = new InfernoUltInfo();

    private InfernoUltInfo() {
        super(InfernoUlt.class, "과부하");
    }
}
