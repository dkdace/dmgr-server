package com.dace.dmgr.combat.ability.info;

import com.dace.dmgr.combat.ability.Trait;
import com.dace.dmgr.item.ItemBuilder;
import lombok.NonNull;

/**
 * 특성 정보를 관리하는 클래스.
 *
 * @param <T> {@link Trait}을 상속받는 특성
 */
public abstract class TraitInfo<T extends Trait> extends AbilityInfo<T> {
    /** 특성 이름의 접두사 */
    private static final String PREFIX = "§b§l[특성] §3";

    /**
     * 특성 정보 인스턴스를 생성한다.
     *
     * @param traitClass      특성 클래스
     * @param name            이름
     * @param abilityInfoLore 능력 정보 설명
     */
    protected TraitInfo(@NonNull Class<@NonNull T> traitClass, @NonNull String name, @NonNull AbilityInfoLore abilityInfoLore) {
        super(traitClass, name, new ItemBuilder(SkillInfo.MATERIAL)
                .setName(PREFIX + name)
                .setDamage((short) 3)
                .build(), abilityInfoLore);
    }
}
