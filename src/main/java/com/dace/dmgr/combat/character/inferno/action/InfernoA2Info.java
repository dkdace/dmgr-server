package com.dace.dmgr.combat.character.inferno.action;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;

public final class InfernoA2Info extends ActiveSkillInfo {
    /** 쿨타임 (tick) */
    public static final int COOLDOWN = 12 * 20;
    /** 초당 화염 피해량 */
    public static final int FIRE_DAMAGE_PER_SECOND = 60;
    /** 효과 범위 (단위: 블록) */
    public static final double RADIUS = 5;
    /** 지속시간 (tick) */
    public static final long DURATION = (long) (2.5 * 20);
    @Getter
    private static final InfernoA2Info instance = new InfernoA2Info();

    private InfernoA2Info() {
        super(2, "불꽃 방출");
    }

    @Override
    @NonNull
    public InfernoA2 createSkill(@NonNull CombatUser combatUser) {
        return new InfernoA2(combatUser);
    }
}
