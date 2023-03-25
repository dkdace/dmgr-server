package com.dace.dmgr.gui.item;

import com.dace.dmgr.gui.ItemBuilder;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

/**
 * 전투 관련 아이템 목록.
 */
public enum CombatItem implements IGuiItem {
    REQ_HEAL((short) 5, "§a치료 요청"),
    SHOW_ULT((short) 5, "§a궁극기 상태"),
    REQ_RALLY((short) 5, "§a집결 요청");

    private final Material MATERIAL = Material.STAINED_GLASS_PANE;
    @Getter
    private final String name;
    @Getter
    private final ItemStack itemStack;

    CombatItem(short damage, String name) {
        this.name = name;
        ItemBuilder itemBuilder = new ItemBuilder(MATERIAL)
                .setDamage(damage)
                .setName(name)
                .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        itemBuilder.getItemMeta().setUnbreakable(true);
        this.itemStack = itemBuilder.build();
    }
}
