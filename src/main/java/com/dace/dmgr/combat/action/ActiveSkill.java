package com.dace.dmgr.combat.action;

public abstract class ActiveSkill extends Skill {
    public ActiveSkill(int number, String name, String... lore) {
        super(number, name, lore);
        itemStack.setDurability((short) 14);
    }
}
