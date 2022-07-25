package com.dace.dmgr.gui.slot;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum ButtonSlot implements ISlotItem {
    EXIT((short) 8, "§c나가기"),
    LEFT((short) 9, "§6이전"),
    RIGHT((short) 10, "§6다음"),
    UP((short) 11, "§6위로"),
    DOWN((short) 12, "§6아래로");

    public final static Material material = Material.CARROT_STICK;
    private final String name;
    private final short damage;

    ButtonSlot(short damage, String name) {
        this.name = name;
        this.damage = damage;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public short getDamage() {
        return damage;
    }
}
