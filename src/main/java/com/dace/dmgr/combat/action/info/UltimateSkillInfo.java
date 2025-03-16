package com.dace.dmgr.combat.action.info;

import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.item.ItemBuilder;
import lombok.NonNull;

/**
 * 궁극기 정보를 관리하는 클래스.
 *
 * @param <T> {@link UltimateSkill}을 상속받는 궁극기
 */
public abstract class UltimateSkillInfo<T extends UltimateSkill> extends ActiveSkillInfo<T> {
    /** 스킬 이름의 접두사 */
    private static final String PREFIX = "§d§l[궁극기] §5";

    /**
     * 궁극기 정보 인스턴스를 생성한다.
     *
     * @param skillClass     궁극기 스킬 클래스
     * @param name           이름
     * @param actionInfoLore 동작 정보 설명
     */
    protected UltimateSkillInfo(@NonNull Class<@NonNull T> skillClass, @NonNull String name, @NonNull ActionInfoLore actionInfoLore) {
        super(skillClass, name, new ItemBuilder(MATERIAL)
                .setName(PREFIX + name)
                .setDamage((short) 10)
                .setGlowing()
                .build(), actionInfoLore);
    }

    @Override
    public String toString() {
        return "§d［" + name + "］";
    }
}
