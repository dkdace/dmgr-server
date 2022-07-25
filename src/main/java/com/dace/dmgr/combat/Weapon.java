package com.dace.dmgr.combat;

import com.dace.dmgr.gui.ItemGenerator;
import com.shampaggon.crackshot.CSUtility;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum Weapon {
    ARKACE(ItemGenerator.getCSItem("HLN-12", "§f" +
            "\n§f뛰어난 안정성을 가진 전자동 돌격소총입니다." +
            "\n§7사격§f하여 §c⚔ 피해§f를 입힙니다." +
            "\n§f"));

    private final ItemStack itemStack;
    private final CSUtility csUtility = new CSUtility();

    Weapon(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    private static class Constants {
        private final static Material material = Material.DIAMOND_HOE;
    }
}
