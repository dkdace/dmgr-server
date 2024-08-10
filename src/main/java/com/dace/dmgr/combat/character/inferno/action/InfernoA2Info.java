package com.dace.dmgr.combat.character.inferno.action;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

public final class InfernoA2Info extends ActiveSkillInfo<InfernoA2> {
    /** 쿨타임 (tick) */
    public static final int COOLDOWN = 12 * 20;
    /** 초당 화염 피해량 */
    public static final int FIRE_DAMAGE_PER_SECOND = 60;
    /** 효과 범위 (단위: 블록) */
    public static final double RADIUS = 5;
    /** 지속시간 (tick) */
    public static final long DURATION = (long) (2.5 * 20);

    /** 초당 효과 점수 */
    public static final int EFFECT_SCORE_PER_SECOND = 3;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 15;
    @Getter
    private static final InfernoA2Info instance = new InfernoA2Info();

    private InfernoA2Info() {
        super(InfernoA2.class, "불꽃 방출");
    }
}
