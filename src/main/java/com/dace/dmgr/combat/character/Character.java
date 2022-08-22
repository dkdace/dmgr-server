package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.Weapon;
import org.bukkit.inventory.ItemStack;

public class Character {
    private final String name;
    private final ItemStack weapon;
    private final IStats stats;
    private final String skinName;

    public Character(String name, Weapon weapon, IStats stats, String skinName) {
        this.name = name;
        this.weapon = weapon.getItemStack();
        this.stats = stats;
        this.skinName = skinName;
    }

    public IStats getStats() {
        return stats;
    }

    public ItemStack getWeapon() {
        return weapon;
    }

    public String getName() {
        return name;
    }

    public String getSkinName() {
        return skinName;
    }
}
