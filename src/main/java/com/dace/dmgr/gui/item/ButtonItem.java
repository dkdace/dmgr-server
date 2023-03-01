package com.dace.dmgr.gui.item;

import com.dace.dmgr.gui.ItemBuilder;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

/**
 * 클릭 가능한 버튼 아이템 목록.
 */
@Getter
public enum ButtonItem implements IGuiItem {
    EXIT((short) 8, "§c§l나가기"),
    LEFT((short) 9, "§6§l이전"),
    RIGHT((short) 10, "§6§l다음"),
    UP((short) 11, "§6§l위로"),
    DOWN((short) 12, "§6§l아래로");

    private final Material MATERIAL = Material.CARROT_STICK;
    private final String name;
    private final ItemStack itemStack;

    ButtonItem(short damage, String name) {
        this.name = name;
        ItemBuilder itemBuilder = new ItemBuilder(MATERIAL)
                .setDamage(damage)
                .setName(name)
                .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        itemBuilder.getItemMeta().setUnbreakable(true);
        this.itemStack = itemBuilder.build();
    }
}
