package com.dace.dmgr.combat.action.info;

import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.NonNull;

/**
 * 액티브 스킬 정보를 관리하는 클래스.
 */
public abstract class ActiveSkillInfo extends SkillInfo {
    /**
     * 액티브 스킬 정보 인스턴스를 생성한다.
     *
     * @param number 스킬 번호
     * @param name   이름
     * @param lores  설명 목록
     */
    protected ActiveSkillInfo(int number, @NonNull String name, @NonNull String @NonNull ... lores) {
        super(number, name, lores);
        itemStack.setDurability((short) 14);
    }

    @Override
    public String toString() {
        return "§c［" + name + "］";
    }

    @Override
    @NonNull
    public abstract ActiveSkill createSkill(@NonNull CombatUser combatUser);
}
