package com.dace.dmgr.combat.action;

import org.bukkit.enchantments.Enchantment;

public abstract class UltimateSkill extends ActiveSkill {
    public UltimateSkill(String name, String... lore) {
        super(4, name, lore);
        itemStack.setDurability((short) 10);
        itemStack.addUnsafeEnchantment(Enchantment.LUCK, 1);
    }

    public int getCost() {
        return 0;
    }

    @Override
    public long getCooldown() {
        return -1;
    }
}
