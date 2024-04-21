package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;

public final class SiliaP2Info extends PassiveSkillInfo {
    /** 벽타기 최대 횟수 */
    public static final int USE_COUNT = 10;
    @Getter
    private static final SiliaP2Info instance = new SiliaP2Info();

    private SiliaP2Info() {
        super(2, "상승 기류");
    }

    @Override
    @NonNull
    public SiliaP2 createSkill(@NonNull CombatUser combatUser) {
        return new SiliaP2(combatUser);
    }
}
