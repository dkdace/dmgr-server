package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

public final class VellionA1Info extends ActiveSkillInfo<VellionA1> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 6 * 20;
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = (int) (1.7 * 20);
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.5 * 20);
    /** 시전 중 이동속도 감소량 */
    public static final int READY_SLOW = 70;
    /** 초당 독 피해량 */
    public static final int POISON_DAMAGE_PER_SECOND = 120;
    /** 초당 치유량 */
    public static final int HEAL_PER_SECOND = 160;
    /** 효과 지속 시간 (tick) */
    public static final long EFFECT_DURATION = (long) (2.5 * 20);
    /** 속박 시간 (tick) */
    public static final long SNARE_DURATION = (long) (0.1 * 20);
    /** 회수 시간 (tick) */
    public static final long RETURN_DURATION = (long) (0.75 * 20);
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 20;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 2.5;

    /** 효과 점수 */
    public static final int EFFECT_SCORE = 1;
    @Getter
    private static final VellionA1Info instance = new VellionA1Info();

    private VellionA1Info() {
        super(VellionA1.class, "마력 집중");
    }
}
