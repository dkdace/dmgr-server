package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;

/**
 * 궁극기 스킬의 상태를 관리하는 클래스.
 */
@Getter
public abstract class UltimateSkill extends Skill {
    protected UltimateSkill(int number, CombatUser combatUser, SkillInfo skillInfo, int slot) {
        super(number, combatUser, skillInfo, slot);
    }

    /**
     * 필요 충전량을 반환한다.
     *
     * @return 필요 충전량
     */
    public abstract int getCost();
}
