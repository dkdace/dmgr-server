package com.dace.dmgr.gui;

import com.dace.dmgr.gui.slot.ButtonSlot;
import com.dace.dmgr.gui.slot.DisplaySlot;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;

public class ItemGenerator {
    public static final Material SLOT_MATERIAL = Material.CARROT_STICK;

    public static ItemStack getItem(Material material, int amount, short damage, String name, String lore) {
        List<String> loreList = Arrays.asList(lore.split("\\n"));
        ItemStack item = new ItemStack(material, amount, damage);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(name);
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemMeta.setLore(loreList);
        item.setItemMeta(itemMeta);

        return item;
    }

    public static ItemStack getItem(Material material, int amount, short damage, String name) {
        ItemStack item = new ItemStack(material, amount, damage);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(name);
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(itemMeta);

        return item;
    }

    public static ItemStack getItem(Material material, int amount, String name, String lore) {
        List<String> loreList = Arrays.asList(lore.split("\\n"));
        ItemStack item = new ItemStack(material, amount);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(name);
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemMeta.setLore(loreList);
        item.setItemMeta(itemMeta);

        return item;
    }

    public static ItemStack getItem(Material material, String name, String lore) {
        List<String> loreList = Arrays.asList(lore.split("\\n"));
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(name);
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemMeta.setLore(loreList);
        item.setItemMeta(itemMeta);

        return item;
    }

    public static ItemStack getItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(name);
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(itemMeta);

        return item;
    }

    public static ItemStack getSlotItem(DisplaySlot type) {
        ItemStack item = ItemGenerator.getItem(SLOT_MATERIAL, 1, type.getDamage(), "??f");
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setUnbreakable(true);
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(itemMeta);

        return item;
    }

    public static ItemStack getSlotItem(ButtonSlot type) {
        ItemStack item = ItemGenerator.getItem(SLOT_MATERIAL, 1, type.getDamage(), type.getName());
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setUnbreakable(true);
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(itemMeta);

        return item;
    }

    public static ItemStack getSlotItem(ButtonSlot type, String name) {
        ItemStack item = ItemGenerator.getItem(SLOT_MATERIAL, 1, type.getDamage(), name);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setUnbreakable(true);
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(itemMeta);

        return item;
    }

    public static ItemStack getPlayerSkull(Player player, String name) {
        ItemStack item = ItemGenerator.getItem(Material.SKULL_ITEM, 1, (short) 3, name);
        SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
        skullMeta.setDisplayName(name);
        skullMeta.setOwningPlayer(player);
        item.setItemMeta(skullMeta);

        return item;
    }
}
