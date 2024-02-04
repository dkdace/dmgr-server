package com.dace.dmgr.item.gui;

import com.dace.dmgr.item.StaticItem;
import org.bukkit.inventory.ItemStack;

/**
 * 정적 아이템 중 GUI에 사용하는 아이템을 관리하는 클래스.
 *
 * @param <E> 식별용 Enum 타입
 * @see Gui
 */
public abstract class GuiItem<E extends Enum<E>> extends StaticItem<E> {
    protected GuiItem(E identifier, ItemStack itemStack) {
        super(identifier, itemStack);
    }

    /**
     * 아이템이 소속된 GUI를 반환한다.
     *
     * @return 소속 GUI. {@code null} 반환 시 모든 GUI에 적용
     */
    public abstract Gui getGui();

    /**
     * GUI에서 클릭할 수 있는 아이템인지 확인한다.
     *
     * @return 클릭 가능 여부
     */
    public abstract boolean isClickable();
}
