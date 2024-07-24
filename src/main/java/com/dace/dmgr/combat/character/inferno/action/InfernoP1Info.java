package com.dace.dmgr.combat.character.inferno.action;

import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;

public final class InfernoP1Info extends PassiveSkillInfo {
    /** 방어력 증가량 */
    public static final int DEFENSE_INCREMENT = 40;
    /** 지속시간 (tick) */
    public static final long DURATION = (long) (1.5 * 20);
    @Getter
    private static final InfernoP1Info instance = new InfernoP1Info();

    private InfernoP1Info() {
        super(1, "불꽃의 용기");
    }

    @Override
    @NonNull
    public InfernoP1 createSkill(@NonNull CombatUser combatUser) {
        return new InfernoP1(combatUser);
    }
}
