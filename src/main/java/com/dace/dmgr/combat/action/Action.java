package com.dace.dmgr.combat.action;

import org.bukkit.inventory.ItemStack;

public class Action {
    protected final String name;
    protected final ItemStack itemStack;

    protected Action(String name, ItemStack itemStack) {
        this.name = name;
        this.itemStack = itemStack;
    }

    public String getName() {
        return name;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
