package com.dace.dmgr.combat.action.info;

import com.dace.dmgr.combat.action.skill.Skill;
import lombok.NonNull;

/**
 * 패시브 스킬 정보를 관리하는 클래스.
 *
 * @param <T> {@link Skill}을 상속받는 스킬
 */
public abstract class PassiveSkillInfo<T extends Skill> extends SkillInfo<T> {
    /**
     * 패시브 스킬 정보 인스턴스를 생성한다.
     *
     * @param skillClass 스킬 클래스
     * @param name       이름
     * @param lores      설명 목록
     */
    protected PassiveSkillInfo(@NonNull Class<@NonNull T> skillClass, @NonNull String name, @NonNull String @NonNull ... lores) {
        super(skillClass, name, lores);
        itemStack.setDurability((short) 4);
    }

    @Override
    public String toString() {
        return "§e［" + name + "］";
    }
}
