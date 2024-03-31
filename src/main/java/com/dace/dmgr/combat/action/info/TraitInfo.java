package com.dace.dmgr.combat.action.info;

import com.dace.dmgr.item.ItemBuilder;
import lombok.Getter;
import org.bukkit.Material;

/**
 * 특성 정보를 관리하는 클래스.
 */
public abstract class TraitInfo extends ActionInfo {
    /** 스킬 아이템 타입 */
    public static final Material MATERIAL = Material.STAINED_GLASS_PANE;
    /** 스킬 이름의 접두사 */
    private static final String PREFIX = "§e§l[특성] §c";
    /** 번호 */
    @Getter
    private final int number;

    protected TraitInfo(int number, String name, String... lore) {
        super(name, new ItemBuilder(MATERIAL)
                .setName(PREFIX + name)
                .setDamage((short) 3)
                .setLore(lore)
                .build());
        this.number = number;
    }

    @Override
    public String toString() {
        return "§9［" + name + "］";
    }
}
