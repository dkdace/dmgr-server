package com.dace.dmgr.gui.item;

import com.dace.dmgr.gui.ItemBuilder;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

/**
 * GUI상에서 빈칸 및 값 등의 표시를 위한 아이템 목록.
 */
public enum DisplayItem implements IGuiItem {
    EMPTY((short) 1),
    EMPTY_LEFT((short) 2),
    EMPTY_RIGHT((short) 3),
    EMPTY_UP((short) 4),
    EMPTY_DOWN((short) 5),
    DISABLED((short) 6),
    ENABLED((short) 7);

    private final Material MATERIAL = Material.CARROT_STICK;
    @Getter
    private final ItemStack itemStack;

    DisplayItem(short damage) {
        ItemBuilder itemBuilder = new ItemBuilder(MATERIAL)
                .setDamage(damage)
                .setName("§f")
                .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        itemBuilder.getItemMeta().setUnbreakable(true);
        this.itemStack = itemBuilder.build();
    }

    @Override
    public String getName() {
        return null;
    }
}
