package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;

public final class SiliaA2Info extends ActiveSkillInfo {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 14 * 20;
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = 1 * 20;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.3 * 20);
    /** 피해량 */
    public static final int DAMAGE = 100;
    /** 이동 강도 */
    public static final double PUSH = 0.8;
    /** 사거리 (단위: 블록) */
    public static final int DISTANCE = 15;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 25;
    /** 투사체 크기 (단위: 블록) */
    public static final double SIZE = 0.8;

    /** 피해 점수 */
    public static final int DAMAGE_SCORE = 8;
    @Getter
    private static final SiliaA2Info instance = new SiliaA2Info();

    private SiliaA2Info() {
        super(2, "진권풍");
    }

    @Override
    @NonNull
    public SiliaA2 createSkill(@NonNull CombatUser combatUser) {
        return new SiliaA2(combatUser);
    }
}
