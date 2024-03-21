package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;

public final class SiliaP1Info extends PassiveSkillInfo {
    @Getter
    private static final SiliaP1Info instance = new SiliaP1Info();

    public SiliaP1Info() {
        super(1, "상승 기류");
    }

    @Override
    public SiliaP1 createSkill(CombatUser combatUser) {
        return new SiliaP1(combatUser);
    }
}
