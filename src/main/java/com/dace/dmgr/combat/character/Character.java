package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.util.SkinUtil;
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
    /** 스킨 */
    private final SkinUtil.Skin skin;
    /** 역할군 */
    private final Role role;
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
     * 전투원으로 매 틱마다 실행할 작업.
     *
     * @param combatUser 대상 플레이어
     * @param i          인덱스
     */
    public void onTick(CombatUser combatUser, long i) {
    }

    /**
     * 전투원으로 다른 엔티티를 공격했을 때 실행할 작업.
     *
     * @param attacker   공격자
     * @param victim     피격자
     * @param damage     피해량
     * @param damageType 피해 타입
     * @param isCrit     치명타 여부
     * @return 궁극기 충전 여부
     * @see Character#onDamage(CombatUser, Attacker, int, DamageType, boolean)
     */
    public boolean onAttack(CombatUser attacker, Damageable victim, int damage, DamageType damageType, boolean isCrit) {
        return true;
    }

    /**
     * 전투원으로 피해를 입었을 때 실행될 작업.
     *
     * @param victim     피격자
     * @param attacker   공격자
     * @param damage     피해량
     * @param damageType 피해 타입
     * @param isCrit     치명타 여부
     * @see Character#onAttack(CombatUser, Damageable, int, DamageType, boolean)
     */
    public void onDamage(CombatUser victim, Attacker attacker, int damage, DamageType damageType, boolean isCrit) {
    }

    /**
     * 전투원으로 다른 엔티티를 치유했을 때 실행될 작업.
     *
     * @param provider 제공자
     * @param target   수급자
     * @param amount   치유량
     * @return 궁극기 충전 여부
     * @see Character#onTakeHeal(CombatUser, Healer, int)
     */
    public boolean onGiveHeal(CombatUser provider, Healable target, int amount) {
        return true;
    }

    /**
     * 전투원으로 치유를 받았을 때 실행될 작업.
     *
     * @param target   수급자
     * @param provider 제공자
     * @param amount   치유량
     * @see Character#onGiveHeal(CombatUser, Healable, int)
     */
    public void onTakeHeal(CombatUser target, Healer provider, int amount) {
    }

    /**
     * 전투원으로 다른 엔티티를 죽였을 때 실행될 작업.
     *
     * @param attacker 공격자
     * @param victim   피격자
     * @see Character#onDeath(CombatUser, Attacker)
     */
    public void onKill(CombatUser attacker, Damageable victim) {
    }

    /**
     * 전투원으로 죽었을 때 실행될 작업.
     *
     * @param victim   피격자
     * @param attacker 공격자
     * @see Character#onKill(CombatUser, Damageable)
     */
    public void onDeath(CombatUser victim, Attacker attacker) {
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
