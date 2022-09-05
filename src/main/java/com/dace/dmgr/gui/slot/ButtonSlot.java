package com.dace.dmgr.gui.slot;

import org.bukkit.Material;

public enum ButtonSlot implements ISlotItem {
    EXIT((short) 8, "§c§l나가기"),
    LEFT((short) 9, "§6§l이전"),
    RIGHT((short) 10, "§6§l다음"),
    UP((short) 11, "§6§l위로"),
    DOWN((short) 12, "§6§l아래로");

    public static final Material MATERIAL = Material.CARROT_STICK;
    private final String name;
    private final short damage;

    ButtonSlot(short damage, String name) {
        this.name = name;
        this.damage = damage;
    }

    @Override
    public Material getMaterial() {
        return MATERIAL;
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
