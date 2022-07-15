package com.dace.dmgr.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Menu {
    private final String name;
    private final int rowSize;
    private final Inventory gui;

    public Menu(int rowSize, String name) {
        if (rowSize > 6) rowSize = 6;
        if (rowSize < 1) rowSize = 1;
        this.name = name;
        this.rowSize = rowSize;
        gui = Bukkit.createInventory(null, rowSize * 9, name);
    }

    public Inventory getGui() {
        return gui;
    }

    protected void fill(ItemStack item) {
        for (int i = 0; i < rowSize * 9; i++) {
            gui.setItem(i, item);
        }
    }

    public void open(Player player) {
        player.openInventory(gui);
    }
}
