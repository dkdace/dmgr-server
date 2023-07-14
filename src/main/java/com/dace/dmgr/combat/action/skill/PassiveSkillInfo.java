package com.dace.dmgr.combat.action.skill;

/**
 * 패시브 스킬 정보를 관리하는 클래스.
 */
public abstract class PassiveSkillInfo extends SkillInfo {
    public PassiveSkillInfo(int number, String name, String... lore) {
        super(number, name, lore);
        itemStack.setDurability((short) 4);
    }
}
