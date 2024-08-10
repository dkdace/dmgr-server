package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

public final class JagerA1Info extends ActiveSkillInfo<JagerA1> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 3 * 20;
    /** 사망 시 쿨타임 (tick) */
    public static final long COOLDOWN_DEATH = 9 * 20;
    /** 소환 최대 거리 (단위: 블록) */
    public static final int SUMMON_MAX_DISTANCE = 15;
    /** 소환 시간 (tick) */
    public static final long SUMMON_DURATION = 1 * 20;
    /** 체력 */
    public static final int HEALTH = 600;
    /** 피해량 */
    public static final int DAMAGE = 150;
    /** 이동속도 */
    public static final double SPEED = 0.45;
    /** 치명상 감지 범위 (단위: 블록) */
    public static final double LOW_HEALTH_DETECT_RADIUS = 20;
    /** 체력 최대 회복 시간 (tick) */
    public static final int RECOVER_DURATION = 6 * 20;

    /** 처치 점수 */
    public static final int KILL_SCORE = 15;
    /** 처치 점수 제한시간 (tick) */
    public static final long KILL_SCORE_TIME_LIMIT = 10 * 20;
    /** 사망 점수 */
    public static final int DEATH_SCORE = 15;
    @Getter
    private static final JagerA1Info instance = new JagerA1Info();

    private JagerA1Info() {
        super(JagerA1.class, "설랑");
    }
}
