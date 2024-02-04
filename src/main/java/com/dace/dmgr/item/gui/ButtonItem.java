package com.dace.dmgr.item.gui;

import com.dace.dmgr.item.ItemBuilder;
import lombok.Getter;
import org.bukkit.Material;

/**
 * GUI에서 사용하는 클릭 가능한 버튼 아이템 목록.
 */
@Getter
public enum ButtonItem {
    /** 나가기 */
    EXIT(8, "§c§l나가기"),
    /** 이전 */
    LEFT(9, "§6§l이전"),
    /** 다음 */
    RIGHT(10, "§6§l다음"),
    /** 위로 */
    UP(11, "§6§l위로"),
    /** 아래로 */
    DOWN(12, "§6§l아래로");

    /** GUI 아이템 객체 */
    private final GuiItem<ButtonItem> guiItem;

    ButtonItem(int damage, String name) {
        ItemBuilder itemBuilder = new ItemBuilder(Material.CARROT_STICK)
                .setDamage((short) damage)
                .setName(name);

        guiItem = new GuiItem<ButtonItem>(this, itemBuilder.build()) {
            @Override
            public Gui getGui() {
                return null;
            }

            @Override
            public boolean isClickable() {
                return true;
            }
        };
    }
}
