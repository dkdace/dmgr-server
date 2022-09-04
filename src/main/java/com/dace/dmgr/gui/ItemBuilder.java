package com.dace.dmgr.gui;

import com.dace.dmgr.gui.slot.ISlotItem;
import com.shampaggon.crackshot.CSUtility;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;

public class ItemBuilder {
    public static final Material SLOT_MATERIAL = Material.CARROT_STICK;
    public static final CSUtility csUtility = new CSUtility();

    private final ItemStack itemStack;
    private final ItemMeta itemMeta;

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
        itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder(Material material) {
        this(new ItemStack(material));
    }

    public static ItemBuilder fromSlotItem(ISlotItem slotItem) {
        ItemBuilder itemBuilder = new ItemBuilder(slotItem.getMaterial())
                .setDamage(slotItem.getDamage())
                .setName(slotItem.getName())
                .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        itemBuilder.getItemMeta().setUnbreakable(true);
        return itemBuilder;
    }

    public static ItemBuilder fromPlayerSkull(Player player) {
        ItemBuilder itemBuilder = new ItemBuilder(Material.SKULL_ITEM).setDamage((short) 3);
        ((SkullMeta) itemBuilder.getItemMeta()).setOwningPlayer(player);
        return itemBuilder;
    }

    public static ItemBuilder fromCSItem(String weaponName) {
        return new ItemBuilder(csUtility.generateWeapon(weaponName)).addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
    }

    public ItemMeta getItemMeta() {
        return itemMeta;
    }

    public ItemBuilder setAmount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    public ItemBuilder setDamage(short damage) {
        itemStack.setDurability(damage);
        return this;
    }

    public ItemBuilder setLore(String... lores) {
        itemMeta.setLore(Arrays.asList(lores));
        return this;
    }

    public ItemBuilder setName(String name) {
        itemMeta.setDisplayName(name);
        return this;
    }

    public ItemBuilder addItemFlags(ItemFlag... itemFlags) {
        itemMeta.addItemFlags(itemFlags);
        return this;
    }

    public ItemBuilder removeItemFlags(ItemFlag... itemFlags) {
        itemMeta.removeItemFlags(itemFlags);
        return this;
    }

    public ItemStack build() {
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
