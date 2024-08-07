package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;

public final class SiliaA1Info extends ActiveSkillInfo {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 8 * 20;
    /** 이동 거리 (단위: 블록) */
    public static final int MOVE_DISTANCE = 15;
    /** 지속시간 (tick) */
    public static final long DURATION = (long) (0.3 * 20);
    /** 피해량 */
    public static final int DAMAGE = 250;
    /** 사거리 (단위: 블록) */
    public static final double DISTANCE = 3;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 2.5;
    @Getter
    private static final SiliaA1Info instance = new SiliaA1Info();

    private SiliaA1Info() {
        super(1, "연풍 가르기");
    }

    @Override
    @NonNull
    public SiliaA1 createSkill(@NonNull CombatUser combatUser) {
        return new SiliaA1(combatUser);
    }
}
