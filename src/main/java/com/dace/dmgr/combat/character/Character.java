package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.action.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 전투원 정보를 관리하는 클래스.
 */
@AllArgsConstructor
@Getter
public abstract class Character {
    /** 이름 */
    private final String name;
    /** 스킨 이름 */
    private final String skinName;
    /** 체력 */
    private final int health;
    /** 이동속도 */
    private final float speed;
    /** 히트박스 크기 계수 */
    private final float hitbox;

    /**
     * @return 상호작용 키 매핑 목록
     */
    public abstract ActionKeyMap getActionKeyMap();

    /**
     * @return 무기 객체
     */
    public abstract Weapon getWeapon();

    /**
     * 지정한 번호의 패시브 스킬을 반환한다.
     *
     * @param number 스킬 번호
     * @return 패시브 스킬 객체
     */
    public abstract PassiveSkill getPassive(int number);

    /**
     * 지정한 번호의 액티브 스킬을 반환한다.
     *
     * @param number 스킬 번호
     * @return 액티브 스킬 객체
     */
    public abstract ActiveSkill getActive(int number);

    /**
     * @return 궁극기 객체
     */
    public abstract UltimateSkill getUltimate();
}
