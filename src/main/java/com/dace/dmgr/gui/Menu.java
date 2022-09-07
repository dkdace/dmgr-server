package com.dace.dmgr.gui;

import com.dace.dmgr.gui.slot.DisplaySlot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class Menu {
    private final int rowSize;
    private final Inventory gui;

    public Menu(int rowSize, String name) {
        if (rowSize > 6) rowSize = 6;
        if (rowSize < 1) rowSize = 1;
        this.rowSize = rowSize;
        gui = Bukkit.createInventory(null, rowSize * 9, name);
    }

    public Inventory getGui() {
        return gui;
    }

    protected void fillAll(ItemStack item) {
        for (int i = 0; i < rowSize * 9; i++) {
            gui.setItem(i, item);
        }
    }

    protected void fillRow(int row, ItemStack item) {
        for (int i = 0; i < 9; i++) {
            gui.setItem((row - 1) * 9 + i, item);
        }
    }

    protected void setToggleButton(int index, ItemStack itemStack, boolean isEnabled) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        String text;
        if (isEnabled)
            text = "§a§l켜짐";
        else
            text = "§c§l꺼짐";

        if (itemMeta.hasLore()) {
            List<String> lore = itemMeta.getLore();
            lore.add(text);
            itemMeta.setLore(lore);
        } else
            itemMeta.setLore(Arrays.asList(text));
        itemStack.setItemMeta(itemMeta);

        gui.setItem(index, itemStack);
    }

    protected void setToggleButton(int index, ItemStack itemStack, boolean isEnabled, int displayIndex) {
        setToggleButton(index, itemStack, isEnabled);
        if (isEnabled)
            gui.setItem(displayIndex, ItemBuilder.fromSlotItem(DisplaySlot.ENABLED).build());
        else
            gui.setItem(displayIndex, ItemBuilder.fromSlotItem(DisplaySlot.DISABLED).build());
    }

    protected void setSelectButton(int index, ItemStack itemStack, boolean isSelected) {
        if (isSelected) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            String text = "§a§l선택됨";

            if (itemMeta.hasLore()) {
                List<String> lore = itemMeta.getLore();
                lore.add(text);
                itemMeta.setLore(lore);
            } else
                itemMeta.setLore(Arrays.asList(text));
            itemStack.setItemMeta(itemMeta);
        }

        gui.setItem(index, itemStack);
    }

    public void open(Player player) {
        player.openInventory(gui);
    }
}
