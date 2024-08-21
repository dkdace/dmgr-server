package com.dace.dmgr.item.gui;

import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.item.StaticItem;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Material;

/**
 * GUI상에서 빈칸 및 값 등의 표시를 위한 아이템 목록.
 */
@Getter
public enum DisplayItem {
    /** 빈 칸 */
    EMPTY(1),
    /** 빈 칸 (왼쪽) */
    EMPTY_LEFT(2),
    /** 빈 칸 (오른쪽) */
    EMPTY_RIGHT(3),
    /** 빈 칸 (위쪽) */
    EMPTY_UP(4),
    /** 빈 칸 (아래쪽) */
    EMPTY_DOWN(5),
    /** 비활성화 */
    DISABLED(6),
    /** 활성화 */
    ENABLED(7);

    /** 정적 아이템 객체 */
    @NonNull
    private final StaticItem staticItem;

    DisplayItem(int damage) {
        ItemBuilder itemBuilder = new ItemBuilder(Material.CARROT_STICK)
                .setDamage((short) damage)
                .setName("§f");

        staticItem = new StaticItem("DisplayItem" + this, itemBuilder.build());
    }
}
