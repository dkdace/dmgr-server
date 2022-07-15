package com.dace.dmgr.gui.slot;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum ButtonSlot {
    EXIT((short) 8, "§c나가기"),
    LEFT((short) 9, "§6이전"),
    RIGHT((short) 10, "§6다음"),
    UP((short) 11, "§6위로"),
    DOWN((short) 12, "§6아래로");

    private final static Material material = Material.CARROT_STICK;
    private final String name;
    private final short damage;

    ButtonSlot(short damage, String name) {
        this.name = name;
        this.damage = damage;
    }

    public static boolean isButtonSlot(ItemStack item) {
        if (item.getType() == material)
            return item.getDurability() >= 8;
        return false;
    }

    public String getName() {
        return name;
    }

    public short getDamage() {
        return damage;
    }
}
