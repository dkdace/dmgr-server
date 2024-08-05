package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;

public final class NeaceP1Info extends PassiveSkillInfo {
    /** 초당 치유량 */
    public static final int HEAL_PER_SECOND = 100;
    /** 활성화 시간 (tick) */
    public static final long ACTIVATE_DURATION = (long) (2.5 * 20);
    @Getter
    private static final NeaceP1Info instance = new NeaceP1Info();

    private NeaceP1Info() {
        super(1, "생명의 힘");
    }

    @Override
    @NonNull
    public NeaceP1 createSkill(@NonNull CombatUser combatUser) {
        return new NeaceP1(combatUser);
    }
}
