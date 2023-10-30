package com.dace.dmgr.gui.item;

import org.bukkit.inventory.ItemStack;

/**
 * GUI에 사용하는 고정 아이템을 관리하는 인터페이스.
 */
public interface GuiItem {
    /**
     * @return 아이템 이름
     */
    String getName();

    /**
     * @return 아이템 객체
     */
    ItemStack getItemStack();
}
