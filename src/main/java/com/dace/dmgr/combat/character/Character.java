package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import com.dace.dmgr.combat.action.info.WeaponInfo;
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
     * @param isFinalHit 결정타 여부. 마지막 공격으로 처치 시 결정타
     * @see Character#onDeath(CombatUser, Attacker)
     */
    public void onKill(@NonNull CombatUser attacker, @NonNull Damageable victim, boolean isFinalHit) {
        // 미사용
    }

    /**
     * 전투원으로 죽었을 때 실행될 작업.
     *
     * @param victim   피격자
     * @param attacker 공격자
     * @see Character#onKill(CombatUser, Damageable, boolean)
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
    public abstract boolean canUseMeleeAttack(@NonNull CombatUser combatUser);

    /**
     * 전투원이 달리기를 할 수 있는 지 확인한다.
     *
     * @param combatUser 대상 플레이어
     * @return 달리기 가능 여부
     */
    public abstract boolean canSprint(@NonNull CombatUser combatUser);

    /**
     * 전투원이 비행할 수 있는 지 확인한다.
     *
     * @param combatUser 대상 플레이어
     * @return 비행 가능 여부
     */
    public abstract boolean canFly(@NonNull CombatUser combatUser);

    /**
     * 전투원이 점프를 할 수 있는 지 확인한다.
     *
     * @param combatUser 대상 플레이어
     * @return 점프 가능 여부
     */
    public abstract boolean canJump(@NonNull CombatUser combatUser);

    /**
     * @return 무기 정보
     */
    @NonNull
    public abstract WeaponInfo getWeaponInfo();

    /**
     * 지정한 번호의 패시브 스킬 정보을 반환한다.
     *
     * @param number 스킬 번호
     * @return 패시브 스킬 정보
     */
    @Nullable
    public abstract PassiveSkillInfo getPassiveSkillInfo(int number);

    /**
     * 지정한 번호의 액티브 스킬 정보을 반환한다.
     *
     * @param number 스킬 번호
     * @return 액티브 스킬 정보
     */
    @Nullable
    public abstract ActiveSkillInfo getActiveSkillInfo(int number);

    /**
     * @return 궁극기 정보
     */
    @NonNull
    public abstract UltimateSkillInfo getUltimateSkillInfo();
}
