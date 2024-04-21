package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;

public final class QuakerA2Info extends ActiveSkillInfo {
    /** 쿨타임 */
    public static final long COOLDOWN = 12 * 20;
    /** 시전 시간 */
    public static final long READY_DURATION = (long) (0.6 * 20);
    /** 피해량 */
    public static final int DAMAGE = 250;
    /** 사거리 */
    public static final double DISTANCE = 10;
    /** 투사체 속력 */
    public static final int VELOCITY = 20;
    /** 기절 시간 */
    public static final long STUN_DURATION = (long) (0.8 * 20);
    /** 이동 속도 감소량 */
    public static final int SLOW = 40;
    /** 이동 속도 감소 시간 */
    public static final long SLOW_DURATION = (long) (2.8 * 20);
    @Getter
    private static final QuakerA2Info instance = new QuakerA2Info();

    private QuakerA2Info() {
        super(2, "충격파 일격");
    }

    @Override
    @NonNull
    public QuakerA2 createSkill(@NonNull CombatUser combatUser) {
        return new QuakerA2(combatUser);
    }
}
