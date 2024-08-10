package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.interaction.DamageType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

/**
 * 전투원 정보를 관리하는 클래스.
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class Character {
    /** 이름 */
    @NonNull
    private final String name;
    /** 스킨 이름 */
    @NonNull
    private final String skinName;
    /** 역할군 */
    @NonNull
    private final Role role;
    /** 전투원 아이콘 */
    private final char icon;
    /** 체력 */
    private final int health;
    /** 이동속도 배수 */
    private final double speedMultiplier;
    /** 히트박스 크기 배수 */
    private final double hitboxMultiplier;

    /**
     * 치료 요청 대사를 반환한다.
     *
     * <p>반환값의 각 인덱스는 다음을 의미한다.</p>
     *
     * <ol>
     * <li>0번째 인덱스 : 치명상일 때의 대사</li>
     * <li>1번째 인덱스 : 체력이 절반 이하일 때의 대사</li>
     * <li>2번째 인덱스 : 체력이 충분할 때의 대사</li>
     * </ol>
     *
     * @return 치료 요청 대사 목록
     */
    @NonNull
    public abstract String @NonNull [] getReqHealMent();

    /**
     * 궁극기 상태 대사를 반환한다.
     *
     * <p>반환값의 각 인덱스는 다음을 의미한다.</p>
     *
     * <ol>
     * <li>0번째 인덱스 : 궁극기 게이지가 0~89%일 때의 대사</li>
     * <li>1번째 인덱스 : 궁극기 게이지가 90~99%일 때의 대사</li>
     * <li>2번째 인덱스 : 궁극기가 충전 상태일 때의 대사</li>
     * </ol>
     *
     * @return 궁극기 상태 대사 목록
     */
    @NonNull
    public abstract String @NonNull [] getUltStateMent();

    /**
     * 집결 요청 대사를 반환한다.
     *
     * @return 집결 요청 대사 목록
     */
    @NonNull
    public abstract String @NonNull [] getReqRallyMent();

    /**
     * 궁극기 사용 대사를 반환한다.
     *
     * @return 궁극기 사용 대사
     */
    @NonNull
    public abstract String getUltUseMent();

    /**
     * 전투원 처치 시 대사를 반환한다.
     *
     * @param characterType 피격자의 전투원 종류
     * @return 전투원 처치 대사 목록
     */
    @NonNull
    public abstract String @NonNull [] getKillMent(@NonNull CharacterType characterType);

    /**
     * 사망 시 대사를 반환한다.
     *
     * @param characterType 공격자의 전투원 종류
     * @return 전투원 사망 대사 목록
     */
    @NonNull
    public abstract String @NonNull [] getDeathMent(@NonNull CharacterType characterType);

    /**
     * 액션바에 무기 및 스킬 상태를 표시하기 위한 문자열을 반환한다.
     *
     * @param combatUser 대상 플레이어
     * @return 액션바 문자열
     */
    @NonNull
    public abstract String getActionbarString(@NonNull CombatUser combatUser);

    /**
     * 전투원으로 매 틱마다 실행할 작업.
     *
     * @param combatUser 대상 플레이어
     * @param i          인덱스
     */
    public void onTick(@NonNull CombatUser combatUser, long i) {
        // 미사용
    }

    /**
     * 전투원이 걸을 때 실행할 작업.
     *
     * <p>주로 발소리 재생에 사용한다.</p>
     *
     * @param combatUser 대상 플레이어
     * @param volume     발소리 음량
     */
    public void onFootstep(@NonNull CombatUser combatUser, double volume) {
        // 미사용
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
     * @see Character#onDamage(CombatUser, Attacker, int, DamageType, Location, boolean)
     */
    public boolean onAttack(@NonNull CombatUser attacker, @NonNull Damageable victim, int damage, @NonNull DamageType damageType, boolean isCrit) {
        return true;
    }

    /**
     * 전투원으로 피해를 입었을 때 실행될 작업.
     *
     * @param victim     피격자
     * @param attacker   공격자
     * @param damage     피해량
     * @param damageType 피해 타입
     * @param location   맞은 위치
     * @param isCrit     치명타 여부
     * @see Character#onAttack(CombatUser, Damageable, int, DamageType, boolean)
     */
    public void onDamage(@NonNull CombatUser victim, @Nullable Attacker attacker, int damage, @NonNull DamageType damageType, Location location, boolean isCrit) {
        // 미사용
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
    public boolean onGiveHeal(@NonNull CombatUser provider, @NonNull Healable target, int amount) {
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
    public void onTakeHeal(@NonNull CombatUser target, @Nullable Healer provider, int amount) {
        // 미사용
    }

    /**
     * 전투원으로 힐 팩을 사용했을 때 실행될 작업.
     *
     * @param combatUser 대상 플레이어
     */
    public void onUseHealPack(@NonNull CombatUser combatUser) {
        // 미사용
    }

    /**
     * 전투원으로 다른 엔티티를 죽였을 때 실행될 작업.
     *
     * @param attacker   공격자
     * @param victim     피격자
     * @param score      점수 (처치 기여도). -1이면 플레이어가 아닌 적
     * @param isFinalHit 결정타 여부. 마지막 공격으로 처치 시 결정타
     * @see Character#onDeath(CombatUser, Attacker)
     */
    public void onKill(@NonNull CombatUser attacker, @NonNull Damageable victim, int score, boolean isFinalHit) {
        // 미사용
    }

    /**
     * 전투원으로 죽었을 때 실행될 작업.
     *
     * @param victim   피격자
     * @param attacker 공격자
     * @see Character#onKill(CombatUser, Damageable, int, boolean)
     */
    public void onDeath(@NonNull CombatUser victim, @Nullable Attacker attacker) {
        // 미사용
    }

    /**
     * 전투원이 기본 근접 공격을 사용할 수 있는 지 확인한다.
     *
     * @param combatUser 대상 플레이어
     * @return 근접 공격을 사용할 수 있으면 {@code true} 반환
     */
    public boolean canUseMeleeAttack(@NonNull CombatUser combatUser) {
        return true;
    }

    /**
     * 전투원이 달리기를 할 수 있는 지 확인한다.
     *
     * @param combatUser 대상 플레이어
     * @return 달리기 가능 여부
     */
    public boolean canSprint(@NonNull CombatUser combatUser) {
        return true;
    }

    /**
     * 전투원이 비행할 수 있는 지 확인한다.
     *
     * @param combatUser 대상 플레이어
     * @return 비행 가능 여부
     */
    public boolean canFly(@NonNull CombatUser combatUser) {
        return false;
    }

    /**
     * 전투원이 점프를 할 수 있는 지 확인한다.
     *
     * @param combatUser 대상 플레이어
     * @return 점프 가능 여부
     */
    public boolean canJump(@NonNull CombatUser combatUser) {
        return true;
    }

    /**
     * @return 무기 정보
     */
    @NonNull
    public abstract WeaponInfo<? extends Weapon> getWeaponInfo();

    /**
     * 지정한 번호의 패시브 스킬 정보을 반환한다.
     *
     * @param number 스킬 번호
     * @return 패시브 스킬 정보. 해당 번호의 스킬이 존재하지 않으면 {@code null} 반환
     */
    @Nullable
    public abstract PassiveSkillInfo<? extends Skill> getPassiveSkillInfo(int number);

    /**
     * 지정한 번호의 액티브 스킬 정보을 반환한다.
     *
     * @param number 스킬 번호
     * @return 액티브 스킬 정보. 해당 번호의 스킬이 존재하지 않으면 {@code null} 반환
     */
    @Nullable
    public abstract ActiveSkillInfo<? extends ActiveSkill> getActiveSkillInfo(int number);

    /**
     * @return 궁극기 정보
     */
    @NonNull
    public abstract UltimateSkillInfo<? extends UltimateSkill> getUltimateSkillInfo();
}
