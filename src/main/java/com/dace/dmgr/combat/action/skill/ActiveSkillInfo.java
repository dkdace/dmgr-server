package com.dace.dmgr.combat.action.skill;

/**
 * 액티브 스킬 정보를 관리하는 클래스.
 */
public abstract class ActiveSkillInfo extends SkillInfo {

    public ActiveSkillInfo(int number, String name, String... lore) {
        super(number, name, lore);
        itemStack.setDurability((short) 14);
    }
}
