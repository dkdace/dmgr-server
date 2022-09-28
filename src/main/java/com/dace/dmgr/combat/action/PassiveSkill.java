package com.dace.dmgr.combat.action;

public abstract class PassiveSkill extends Skill {
    public PassiveSkill(int number, String name, String... lore) {
        super(number, name, lore);
        itemStack.setDurability((short) 4);
    }
}
