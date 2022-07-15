package com.dace.dmgr.gui.slot;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum DisplaySlot {
    EMPTY((short) 1),
    EMPTY_LEFT((short) 2),
    EMPTY_RIGHT((short) 3),
    EMPTY_UP((short) 4),
    EMPTY_DOWN((short) 5),
    DISABLED((short) 6),
    ENABLED((short) 7);

    private final static Material material = Material.CARROT_STICK;
    private final short damage;

    DisplaySlot(short damage) {
        this.damage = damage;
    }

    public static boolean isDisplaySlot(ItemStack item) {
        if (item.getType() == material)
            return item.getDurability() <= 7;
        return false;
    }

    public short getDamage() {
        return damage;
    }
}
