package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.action.ActionKeyMap;
import com.dace.dmgr.combat.action.skill.ActiveSkillInfo;
import com.dace.dmgr.combat.action.skill.PassiveSkillInfo;
import com.dace.dmgr.combat.action.skill.UltimateSkillInfo;
import com.dace.dmgr.combat.action.weapon.WeaponInfo;
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
     * @return 무기 정보
     */
    public abstract WeaponInfo getWeaponInfo();

    /**
     * 지정한 번호의 패시브 스킬 정보을 반환한다.
     *
     * @param number 스킬 번호
     * @return 패시브 스킬 정보
     */
    public abstract PassiveSkillInfo getPassiveSkillInfo(int number);

    /**
     * 지정한 번호의 액티브 스킬 정보을 반환한다.
     *
     * @param number 스킬 번호
     * @return 액티브 스킬 정보
     */
    public abstract ActiveSkillInfo getActiveSkillInfo(int number);

    /**
     * @return 궁극기 정보
     */
    public abstract UltimateSkillInfo getUltimateSkillInfo();
}
