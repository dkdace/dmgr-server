package com.dace.dmgr.combat.action.info;

import com.dace.dmgr.item.ItemBuilder;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Material;

/**
 * 특성 정보를 관리하는 클래스.
 */
@Getter
public abstract class TraitInfo extends ActionInfo {
    /** 특성 아이템 타입 */
    public static final Material MATERIAL = Material.STAINED_GLASS_PANE;
    /** 특성 이름의 접두사 */
    private static final String PREFIX = "§e§l[특성] §c";
    /** 특성 번호 */
    private final int number;

    /**
     * 특성 정보 인스턴스를 생성한다.
     *
     * @param number 특성 번호
     * @param name   이름
     * @param lores  설명 목록
     */
    protected TraitInfo(int number, @NonNull String name, @NonNull String @NonNull ... lores) {
        super(name, new ItemBuilder(MATERIAL)
                .setName(PREFIX + name)
                .setDamage((short) 3)
                .setLore(lores)
                .build());
        this.number = number;
    }

    @Override
    public String toString() {
        return "§9［" + name + "］";
    }
}
