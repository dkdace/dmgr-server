package com.dace.dmgr.item.gui;

import com.dace.dmgr.item.StaticItem;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * 정적 아이템 중 GUI에 사용하는 아이템을 관리하는 클래스.
 *
 * @see Gui
 */
public abstract class GuiItem extends StaticItem {
    protected GuiItem(@NonNull String identifier, @NonNull ItemStack itemStack) {
        super(identifier, itemStack);
    }

    protected GuiItem(@NonNull String identifier, @NonNull StaticItem staticItem) {
        super(identifier, staticItem.getItemStack());
    }

    /**
     * 아이템을 클릭했을 때 실행할 작업.
     *
     * @param clickType 클릭 유형
     * @param clickItem 클릭한 아이템 객체
     * @param player    클릭한 플레이어
     * @return 클릭 성공 여부
     */
    public abstract boolean onClick(@NonNull ClickType clickType, @NonNull ItemStack clickItem, @NonNull Player player);
}
