package com.dace.dmgr.combat.action.info;

import com.dace.dmgr.combat.action.skill.ActiveSkill;
import lombok.NonNull;

/**
 * 액티브 스킬 정보를 관리하는 클래스.
 *
 * @param <T> {@link ActiveSkill}을 상속받는 액티브 스킬
 */
public abstract class ActiveSkillInfo<T extends ActiveSkill> extends SkillInfo<T> {
    /**
     * 액티브 스킬 정보 인스턴스를 생성한다.
     *
     * @param skillClass 액티브 스킬 클래스
     * @param name       이름
     * @param lores      설명 목록
     */
    protected ActiveSkillInfo(@NonNull Class<@NonNull T> skillClass, @NonNull String name, @NonNull String @NonNull ... lores) {
        super(skillClass, name, lores);
        itemStack.setDurability((short) 14);
    }

    @Override
    public String toString() {
        return "§c［" + name + "］";
    }
}
