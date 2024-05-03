package com.dace.dmgr.combat.action.info;

import lombok.NonNull;

/**
 * 패시브 스킬 정보를 관리하는 클래스.
 */
public abstract class PassiveSkillInfo extends SkillInfo {
    /**
     * 패시브 스킬 정보 인스턴스를 생성한다.
     *
     * @param number 스킬 번호
     * @param name   이름
     * @param lores  설명 목록
     */
    protected PassiveSkillInfo(int number, @NonNull String name, @NonNull String @NonNull ... lores) {
        super(number, name, lores);
        itemStack.setDurability((short) 4);
    }

    @Override
    public String toString() {
        return "§e［" + name + "］";
    }
}
