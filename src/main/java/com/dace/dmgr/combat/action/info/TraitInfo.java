package com.dace.dmgr.combat.action.info;

import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.item.StaticItem;
import lombok.Getter;
import lombok.NonNull;

/**
 * 특성 정보를 관리하는 클래스.
 */
@Getter
public class TraitInfo extends ActionInfo {
    /** 특성 이름의 접두사 */
    private static final String PREFIX = "§b§l[특성] §3";

    /**
     * 특성 정보 인스턴스를 생성한다.
     *
     * @param name  이름
     * @param lores 설명 목록
     */
    protected TraitInfo(@NonNull String name, @NonNull String @NonNull ... lores) {
        super(name, new StaticItem("TraitInfo" + name, new ItemBuilder(SkillInfo.MATERIAL)
                .setName(PREFIX + name)
                .setDamage((short) 3)
                .setLore(lores)
                .build()));
    }

    @Override
    public String toString() {
        return "§b［" + name + "］";
    }
}
