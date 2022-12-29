package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.action.*;

/**
 * 전투원 정보를 관리하는 인터페이스.
 */
public interface ICharacter {
    String getName();

    String getSkinName();

    int getHealth();

    float getSpeed();

    float getHitbox();

    ActionKeyMap getActionKeyMap();

    Weapon getWeapon();

    /**
     * 지정한 번호의 패시브 스킬을 반환한다.
     *
     * @param number 스킬 번호
     * @return 패시브 스킬 객체
     */
    default PassiveSkill getPassive(int number) {
        return null;
    }

    /**
     * 지정한 번호의 액티브 스킬을 반환한다.
     *
     * @param number 스킬 번호
     * @return 액티브 스킬 객체
     */
    default ActiveSkill getActive(int number) {
        return null;
    }

    UltimateSkill getUltimate();
}
