package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.skill.PassiveSkillInfo;
import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;

public final class JagerP1Info extends PassiveSkillInfo {
    /** 이동속도 증가량 */
    public static final int SPEED = 15;
    /** 감지 범위 */
    public static final int DETECT_RADIUS = 10;
    @Getter
    private static final JagerP1Info instance = new JagerP1Info();

    public JagerP1Info() {
        super(1, "사냥의 미학");
    }

    @Override
    public Skill createSkill(CombatUser combatUser) {
        return new JagerP1(combatUser);
    }
}
