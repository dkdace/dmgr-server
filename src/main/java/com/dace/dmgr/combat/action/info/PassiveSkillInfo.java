package com.dace.dmgr.combat.action.info;

import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.item.ItemBuilder;
import lombok.NonNull;

/**
 * 패시브 스킬 정보를 관리하는 클래스.
 *
 * @param <T> {@link Skill}을 상속받는 스킬
 */
public abstract class PassiveSkillInfo<T extends Skill> extends SkillInfo<T> {
    /** 스킬 이름의 접두사 */
    private static final String PREFIX = "§e§l[패시브 스킬] §6";

    /**
     * 패시브 스킬 정보 인스턴스를 생성한다.
     *
     * @param skillClass     스킬 클래스
     * @param name           이름
     * @param actionInfoLore 동작 정보 설명
     */
    protected PassiveSkillInfo(@NonNull Class<@NonNull T> skillClass, @NonNull String name, @NonNull ActionInfoLore actionInfoLore) {
        super(skillClass, name, new ItemBuilder(MATERIAL)
                .setName(PREFIX + name)
                .setDamage((short) 4)
                .setLore(actionInfoLore.toString())
                .build());
    }

    @Override
    public String toString() {
        return "§e［" + name + "］";
    }
}
