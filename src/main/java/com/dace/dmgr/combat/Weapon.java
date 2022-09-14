package com.dace.dmgr.combat;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Weapon {
    private final static Material material = Material.DIAMOND_HOE;
    private static final String PREFIX = "§e§l[기본무기] §f";

    private final ItemStack itemStack;

    public Weapon(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
