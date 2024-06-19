package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;

public final class VellionA3Info extends ActiveSkillInfo {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 17 * 20;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.6 * 20);
    /** 시전 중 이동속도 감소량 */
    public static final int READY_SLOW = 70;
    /** 효과 범위 (단위: 블록) */
    public static final double RADIUS = 5;
    /** 최대 거리 (단위: 블록) */
    public static final int MAX_DISTANCE = 30;
    /** 지속시간 (tick) */
    public static final long DURATION = 6 * 20;

    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 20;
    @Getter
    private static final VellionA3Info instance = new VellionA3Info();

    private VellionA3Info() {
        super(3, "칠흑의 균열");
    }

    @Override
    @NonNull
    public VellionA3 createSkill(@NonNull CombatUser combatUser) {
        return new VellionA3(combatUser);
    }
}
