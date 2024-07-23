package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;

public final class NeaceUltInfo extends UltimateSkillInfo {
    /** 궁극기 필요 충전량 */
    public static final int COST = 6000;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.8 * 20);
    /** 시전 중 이동속도 감소량 */
    public static final int READY_SLOW = 70;
    /** 지속시간 (tick) */
    public static final long DURATION = (long) 12 * 20;
    @Getter
    private static final NeaceUltInfo instance = new NeaceUltInfo();

    private NeaceUltInfo() {
        super("치유의 성역");
    }

    @Override
    @NonNull
    public NeaceUlt createSkill(@NonNull CombatUser combatUser) {
        return new NeaceUlt(combatUser);
    }
}
