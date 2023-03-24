package com.dace.dmgr.util;

import com.dace.dmgr.gui.item.DisplayItem;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * 인벤토리 내부의 아이템 관련 기능을 제공하는 클래스.
 */
public class InventoryUtil {
    /**
     * 지정한 인벤토리의 모든 칸을 특정 아이템으로 채운다.
     *
     * @param inventory 대상 인벤토리
     * @param itemStack 아이템
     */
    public static void fillAll(Inventory inventory, ItemStack itemStack) {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, itemStack);
        }
    }

    /**
     * 지정한 인벤토리의 행을 특정 아이템으로 채운다.
     *
     * @param inventory 대상 인벤토리
     * @param row       행 번호. {@code 1 ~ 6} 사이의 값
     * @param itemStack 대상 아이템
     */
    public static void fillRow(Inventory inventory, int row, ItemStack itemStack) {
        for (int i = 0; i < 9; i++) {
            inventory.setItem((row - 1) * 9 + i, itemStack);
        }
    }

    /**
     * 지정한 인벤토리의 칸에 클릭하여 활성화할 수 있는 버튼 아이템을 배치한다.
     *
     * @param inventory 대상 인벤토리
     * @param index     칸 번호
     * @param itemStack 대상 아이템
     * @param isEnabled 활성화 여부. {@code true}로 지정하면 아이템 설명에 '켜짐',
     *                  {@code false}면 '꺼짐'이 추가된다.
     */
    public static void setToggleButton(Inventory inventory, int index, ItemStack itemStack, boolean isEnabled) {
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

        inventory.setItem(index, itemStack);
    }

    /**
     * 지정한 인벤토리의 칸에 클릭하여 활성화할 수 있는 버튼 아이템과 활성화 여부를 표시하는 아이템을 배치한다.
     *
     * @param inventory    대상 인벤토리
     * @param index        칸 번호
     * @param itemStack    대상 아이템
     * @param isEnabled    활성화 여부. {@code true}로 지정하면 아이템 설명에 '켜짐',
     *                     {@code false}면 '꺼짐'이 추가된다.
     * @param displayIndex 활성화 여부를 표시할 칸 번호.
     */
    public static void setToggleButton(Inventory inventory, int index, ItemStack itemStack, boolean isEnabled, int displayIndex) {
        setToggleButton(inventory, index, itemStack, isEnabled);
        if (isEnabled)
            inventory.setItem(displayIndex, DisplayItem.ENABLED.getItemStack());
        else
            inventory.setItem(displayIndex, DisplayItem.DISABLED.getItemStack());
    }

    /**
     * 지정한 인벤토리의 칸에 클릭하여 선택할 수 있는 아이템을 배치한다.
     *
     * @param inventory  대상 인벤토리
     * @param index      칸 번호
     * @param itemStack  대상 아이템
     * @param isSelected 선택 여부. {@code true}로 지정하면 아이템 설명에 '선택됨'이 추가된다.
     */
    public static void setSelectButton(Inventory inventory, int index, ItemStack itemStack, boolean isSelected) {
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

        inventory.setItem(index, itemStack);
    }
}
