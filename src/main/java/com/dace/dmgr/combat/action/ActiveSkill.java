package com.dace.dmgr.combat.action;

/**
 * 액티브 스킬 정보를 관리하는 클래스.
 */
public abstract class ActiveSkill extends Skill {

    public ActiveSkill(int number, String name, String... lore) {
        super(number, name, lore);
    }
}
