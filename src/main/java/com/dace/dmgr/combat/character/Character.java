package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.Weapon;

public abstract class Character implements ICharacter {
    private final String name;
    private final Weapon weapon;
    private final IStats stats;
    private final String skinName;

    public Character(String name, Weapon weapon, IStats stats, String skinName) {
        this.name = name;
        this.weapon = weapon;
        this.stats = stats;
        this.skinName = skinName;
    }

    public IStats getStats() {
        return stats;
    }

    public Weapon getWeapon() {
        return weapon;
    }

    public String getName() {
        return name;
    }

    public String getSkinName() {
        return skinName;
    }
}
