package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;

public final class QuakerUltInfo extends UltimateSkillInfo {
    /** 궁극기 필요 충전량 */
    public static final int COST = 7000;
    /** 시전 시간 */
    public static final long READY_DURATION = (long) (0.5 * 20);
    /** 피해량 */
    public static final int DAMAGE = 250;
    /** 사거리 */
    public static final double DISTANCE = 12;
    /** 투사체 속력 */
    public static final int VELOCITY = 25;
    /** 기절 시간 */
    public static final long STUN_DURATION = 1 * 20;
    /** 이동 속도 감소량 */
    public static final int SLOW = 30;
    /** 이동 속도 감소 시간 */
    public static final long SLOW_DURATION = 12 * 20;
    @Getter
    private static final QuakerUltInfo instance = new QuakerUltInfo();

    public QuakerUltInfo() {
        super("심판의 문지기");
    }

    @Override
    public @NonNull QuakerUlt createSkill(@NonNull CombatUser combatUser) {
        return new QuakerUlt(combatUser);
    }
}
