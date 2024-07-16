package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;

public final class QuakerUltInfo extends UltimateSkillInfo {
    /** 궁극기 필요 충전량 */
    public static final int COST = 7000;
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = (int) (0.8 * 20);
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.5 * 20);
    /** 피해량 */
    public static final int DAMAGE = 250;
    /** 사거리 (단위: 블록) */
    public static final double DISTANCE = 12;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 25;
    /** 판정 크기 (단위: 블록) */
    public static final double SIZE = 1;
    /** 기절 시간 (tick) */
    public static final long STUN_DURATION = 1 * 20;
    /** 이동 속도 감소량 */
    public static final int SLOW = 30;
    /** 이동 속도 감소 시간 (tick) */
    public static final long SLOW_DURATION = 12 * 20;
    /** 넉백 강도 */
    public static final double KNOCKBACK = 3;

    /** 피해 점수 */
    public static final int DAMAGE_SCORE = 15;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 30;
    @Getter
    private static final QuakerUltInfo instance = new QuakerUltInfo();

    private QuakerUltInfo() {
        super("심판의 문지기");
    }

    @Override
    @NonNull
    public QuakerUlt createSkill(@NonNull CombatUser combatUser) {
        return new QuakerUlt(combatUser);
    }
}
