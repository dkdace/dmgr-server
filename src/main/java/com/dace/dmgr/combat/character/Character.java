package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.action.skill.ActiveSkillInfo;
import com.dace.dmgr.combat.action.skill.PassiveSkillInfo;
import com.dace.dmgr.combat.action.skill.UltimateSkillInfo;
import com.dace.dmgr.combat.action.weapon.WeaponInfo;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
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
    /** 이동속도 계수 */
    private final float speedMultiplier;
    /** 히트박스 크기 계수 */
    private final float hitboxMultiplier;

    /**
     * 액션바에 무기 및 스킬 상태를 표시하기 위한 문자열을 반환한다.
     *
     * @param combatUser 대상 플레이어
     * @return 액션바 문자열
     */
    public abstract String getActionbarString(CombatUser combatUser);

    /**
     * 전투원으로 다른 엔티티를 공격했을 때 실행할 작업.
     *
     * @param attacker 공격자
     * @param victim   피격자
     * @param damage   피해량
     * @param type     타입
     * @param isCrit   치명타 여부
     * @param isUlt    궁극기 충전 여부
     * @see Character#onDamage(CombatUser, CombatEntity, int, String, boolean, boolean)
     */
    public void onAttack(CombatUser attacker, CombatEntity<?> victim, int damage, String type, boolean isCrit, boolean isUlt) {
    }

    /**
     * 전투원으로 피해를 입었을 때 실행될 작업.
     *
     * @param victim   피격자
     * @param attacker 공격자
     * @param damage   피해량
     * @param type     타입
     * @param isCrit   치명타 여부
     * @param isUlt    궁극기 충전 여부
     * @see Character#onAttack(CombatUser, CombatEntity, int, String, boolean, boolean)
     */
    public void onDamage(CombatUser victim, CombatEntity<?> attacker, int damage, String type, boolean isCrit, boolean isUlt) {
    }

    /**
     * 전투원으로 다른 엔티티를 치유했을 때 실행될 작업.
     *
     * @param attacker 공격자
     * @param victim   피격자
     * @param amount   치유량
     * @param isUlt    궁극기 충전 여부
     * @see Character#onTakeHeal(CombatUser, CombatEntity, int, boolean)
     */
    public void onGiveHeal(CombatUser attacker, CombatEntity<?> victim, int amount, boolean isUlt) {
    }

    /**
     * 전투원으로 치유를 받았을 때 실행될 작업.
     *
     * @param victim   피격자
     * @param attacker 공격자
     * @param amount   치유량
     * @param isUlt    궁극기 충전 여부
     * @see Character#onGiveHeal(CombatUser, CombatEntity, int, boolean)
     */
    public void onTakeHeal(CombatUser victim, CombatEntity<?> attacker, int amount, boolean isUlt) {
    }

    /**
     * 전투원으로 다른 엔티티를 죽였을 때 실행될 작업.
     *
     * @param attacker 공격자
     * @param victim   피격자
     * @see Character#onDeath(CombatUser, CombatEntity)
     */
    public void onKill(CombatUser attacker, CombatEntity<?> victim) {
    }

    /**
     * 전투원으로 죽었을 때 실행될 작업.
     *
     * @param victim   피격자
     * @param attacker 공격자
     * @see Character#onKill(CombatUser, CombatEntity)
     */
    public void onDeath(CombatUser victim, CombatEntity<?> attacker) {
    }

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
