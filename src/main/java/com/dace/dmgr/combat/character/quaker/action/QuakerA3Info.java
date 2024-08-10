package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

public final class QuakerA3Info extends ActiveSkillInfo<QuakerA3> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 10 * 20;
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = (int) (0.8 * 20);
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.4 * 20);
    /** 피해량 */
    public static final int DAMAGE = 200;
    /** 사거리 (단위: 블록) */
    public static final double DISTANCE = 40;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 10;
    /** 투사체 크기 (단위: 블록) */
    public static final double SIZE = 2.5;
    /** 속박 시간 (tick) */
    public static final long SNARE_DURATION = (long) (0.3 * 20);
    /** 넉백 강도 */
    public static final double KNOCKBACK = 2;

    /** 피해 점수 */
    public static final int DAMAGE_SCORE = 6;
    @Getter
    private static final QuakerA3Info instance = new QuakerA3Info();

    private QuakerA3Info() {
        super(QuakerA3.class, "돌풍 강타");
    }
}
