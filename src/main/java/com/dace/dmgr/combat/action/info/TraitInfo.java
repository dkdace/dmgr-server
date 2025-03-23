package com.dace.dmgr.combat.action.info;

import com.dace.dmgr.item.ItemBuilder;
import lombok.NonNull;

/**
 * 특성 정보를 관리하는 클래스.
 */
public class TraitInfo extends ActionInfo {
    /** 특성 이름의 접두사 */
    private static final String PREFIX = "§b§l[특성] §3";

    /**
     * 특성 정보 인스턴스를 생성한다.
     *
     * @param name           이름
     * @param actionInfoLore 동작 정보 설명
     */
    protected TraitInfo(@NonNull String name, @NonNull ActionInfoLore actionInfoLore) {
        super(name, new ItemBuilder(SkillInfo.MATERIAL)
                .setName(PREFIX + name)
                .setDamage((short) 3)
                .build(), actionInfoLore);
    }

    @Override
    public String toString() {
        return "§b［" + name + "］";
    }
}
