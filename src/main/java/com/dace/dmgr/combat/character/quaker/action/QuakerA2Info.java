package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

public final class QuakerA2Info extends ActiveSkillInfo<QuakerA2> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 12 * 20;
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = 1 * 20;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.6 * 20);
    /** 피해량 */
    public static final int DAMAGE = 250;
    /** 사거리 (단위: 블록) */
    public static final double DISTANCE = 10;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 20;
    /** 판정 크기 (단위: 블록) */
    public static final double SIZE = 1.2;
    /** 기절 시간 (tick) */
    public static final long STUN_DURATION = (long) (0.8 * 20);
    /** 이동 속도 감소량 */
    public static final int SLOW = 40;
    /** 이동 속도 감소 시간 (tick) */
    public static final long SLOW_DURATION = (long) (2.8 * 20);

    /** 피해 점수 */
    public static final int DAMAGE_SCORE = 10;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 25;
    @Getter
    private static final QuakerA2Info instance = new QuakerA2Info();

    private QuakerA2Info() {
        super(QuakerA2.class, "충격파 일격");
    }
}
