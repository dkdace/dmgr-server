package com.dace.dmgr.combat.ability.info;

import com.dace.dmgr.combat.ability.skill.PassiveSkill;
import com.dace.dmgr.item.ItemBuilder;
import lombok.NonNull;

/**
 * 패시브 스킬 정보를 관리하는 클래스.
 *
 * @param <T> {@link PassiveSkill}을 상속받는 패시브 스킬
 */
public abstract class PassiveSkillInfo<T extends PassiveSkill> extends SkillInfo<T> {
    /** 스킬 이름의 접두사 */
    private static final String PREFIX = "§e§l[패시브 스킬] §6";

    /**
     * 패시브 스킬 정보 인스턴스를 생성한다.
     *
     * @param skillClass      패시브 스킬 클래스
     * @param name            이름
     * @param abilityInfoLore 능력 정보 설명
     */
    protected PassiveSkillInfo(@NonNull Class<@NonNull T> skillClass, @NonNull String name, @NonNull AbilityInfoLore abilityInfoLore) {
        super(skillClass, name, new ItemBuilder(MATERIAL)
                .setName(PREFIX + name)
                .setDamage((short) 4)
                .build(), abilityInfoLore);
    }
}
