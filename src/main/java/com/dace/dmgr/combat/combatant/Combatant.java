package com.dace.dmgr.combat.combatant;

import com.dace.dmgr.combat.action.info.*;
import com.dace.dmgr.combat.action.weapon.Swappable;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.entity.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * 전투원 정보를 관리하는 클래스.
 *
 * @see Scuffler
 * @see Marksman
 * @see Vanguard
 * @see Guardian
 * @see Support
 * @see Controller
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public abstract class Combatant {
    /** 이름 */
    @NonNull
    private final String name;
    /** 별명 */
    @NonNull
    private final String nickname;
    /** 스킨 이름 */
    @NonNull
    private final String skinName;
    /** 주 역할군 */
    @NonNull
    private final Role role;
    /** 부 역할군 */
    @Nullable
    private final Role subRole;
    /** 전투원 아이콘 */
    private final char icon;
    /** 난이도 */
    private final int difficulty;
    /** 체력 */
    private final int health;
    /** 이동속도 배수 */
    private final double speedMultiplier;
    /** 히트박스 크기 배수 */
    private final double hitboxMultiplier;

    /**
     * 치명상일 때의 치료 요청 대사를 반환한다.
     *
     * @return 치료 요청 대사
     */
    @NonNull
    public abstract String getReqHealMentLow();

    /**
     * 체력이 절반 이하일 때의 치료 요청 대사를 반환한다.
     *
     * @return 치료 요청 대사
     */
    @NonNull
    public abstract String getReqHealMentHalf();

    /**
     * 체력이 충분할 때의 치료 요청 대사를 반환한다.
     *
     * @return 치료 요청 대사
     */
    @NonNull
    public abstract String getReqHealMentNormal();

    /**
     * 궁극기 게이지가 0~89%일 때의 궁극기 상태 대사를 반환한다.
     *
     * @return 궁극기 상태 대사
     */
    @NonNull
    public abstract String getUltStateMentLow();

    /**
     * 궁극기 게이지가 90~99%일 때의 궁극기 상태 대사를 반환한다.
     *
     * @return 궁극기 상태 대사
     */
    @NonNull
    public abstract String getUltStateMentNearFull();

    /**
     * 궁극기 게이지가 충전 상태일 때의 궁극기 상태 대사를 반환한다.
     *
     * @return 궁극기 상태 대사
     */
    @NonNull
    public abstract String getUltStateMentFull();

    /**
     * 집결 요청 대사 목록을 반환한다.
     *
     * @return 집결 요청 대사 목록
     */
    @NonNull
    public abstract String @NonNull [] getReqRallyMents();

    /**
     * 궁극기 사용 대사를 반환한다.
     *
     * @return 궁극기 사용 대사
     */
    @NonNull
    public abstract String getUltUseMent();

    /**
     * 전투원 처치 시 대사 목록을 반환한다.
     *
     * @param combatantType 피격자의 전투원 종류
     * @return 전투원 처치 대사 목록
     */
    @NonNull
    public abstract String @NonNull [] getKillMent(@NonNull CombatantType combatantType);

    /**
     * 사망 시 대사 목록을 반환한다.
     *
     * @param combatantType 공격자의 전투원 종류
     * @return 전투원 사망 대사 목록
     */
    @NonNull
    public abstract String @NonNull [] getDeathMent(@NonNull CombatantType combatantType);

    /**
     * 액션바에 무기 및 스킬 상태를 표시하기 위한 문자열을 반환한다.
     *
     * @param combatUser 대상 플레이어
     * @return 액션바 문자열
     */
    @NonNull
    public final String getActionBarString(@NonNull CombatUser combatUser) {
        ArrayList<String> texts = new ArrayList<>();

        Weapon weapon = combatUser.getWeapon();
        String weaponText = weapon.getActionBarString();
        if (weaponText != null) {
            texts.add(weaponText);

            if (weapon instanceof Swappable) {
                Weapon subweapon = ((Swappable<?>) weapon).getSwapModule().getSubweapon();
                String subweaponText = subweapon.getActionBarString();
                if (subweaponText != null)
                    texts.add(subweaponText);
            }

            texts.add("");
        }

        for (SkillInfo<?> skillInfo : getSkillInfos()) {
            String actionBarString = combatUser.getSkill(skillInfo).getActionBarString();
            if (actionBarString != null)
                texts.add(actionBarString);
        }

        return String.join("    ", texts);
    }

    /**
     * 전투원을 선택했을 때 실행할 작업.
     *
     * @param combatUser 대상 플레이어
     */
    public void onSet(@NonNull CombatUser combatUser) {
        // 미사용
    }

    /**
     * 전투원이 매 틱마다 실행할 작업.
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
     * 전투원이 다른 엔티티를 공격했을 때 실행할 작업.
     *
     * @param attacker 공격자
     * @param victim   피격자
     * @param damage   피해량
     * @param isCrit   치명타 여부
     * @return 궁극기 충전 여부
     * @see Combatant#onDamage(CombatUser, Attacker, double, Location, boolean)
     */
    public boolean onAttack(@NonNull CombatUser attacker, @NonNull Damageable victim, double damage, boolean isCrit) {
        return true;
    }

    /**
     * 전투원이 피해를 입었을 때 실행될 작업.
     *
     * @param victim   피격자
     * @param attacker 공격자
     * @param damage   피해량
     * @param location 맞은 위치
     * @param isCrit   치명타 여부
     * @see Combatant#onAttack(CombatUser, Damageable, double, boolean)
     */
    public void onDamage(@NonNull CombatUser victim, @Nullable Attacker attacker, double damage, @Nullable Location location, boolean isCrit) {
        // 미사용
    }

    /**
     * 전투원이 다른 엔티티를 치유했을 때 실행될 작업.
     *
     * @param provider 제공자
     * @param target   수급자
     * @param amount   치유량
     * @return 궁극기 충전 여부
     * @see Combatant#onTakeHeal(CombatUser, Healer, double)
     */
    public boolean onGiveHeal(@NonNull CombatUser provider, @NonNull Healable target, double amount) {
        return true;
    }

    /**
     * 전투원이 치유를 받았을 때 실행될 작업.
     *
     * @param target   수급자
     * @param provider 제공자
     * @param amount   치유량
     * @see Combatant#onGiveHeal(CombatUser, Healable, double)
     */
    public void onTakeHeal(@NonNull CombatUser target, @Nullable Healer provider, double amount) {
        // 미사용
    }

    /**
     * 전투원이 힐 팩을 사용했을 때 실행될 작업.
     *
     * @param combatUser 대상 플레이어
     */
    public void onUseHealPack(@NonNull CombatUser combatUser) {
        // 미사용
    }

    /**
     * 전투원이 다른 엔티티를 죽였을 때 실행될 작업.
     *
     * @param attacker   공격자
     * @param victim     피격자
     * @param score      점수 (처치 기여도). -1이면 플레이어가 아닌 적
     * @param isFinalHit 결정타 여부. 마지막 공격으로 처치 시 결정타
     * @see Combatant#onDeath(CombatUser, Attacker)
     */
    public void onKill(@NonNull CombatUser attacker, @NonNull Damageable victim, int score, boolean isFinalHit) {
        // 미사용
    }

    /**
     * 전투원이 죽었을 때 실행될 작업.
     *
     * @param victim   피격자
     * @param attacker 공격자
     * @see Combatant#onKill(CombatUser, Damageable, int, boolean)
     */
    public void onDeath(@NonNull CombatUser victim, @Nullable Attacker attacker) {
        // 미사용
    }

    /**
     * 전투원이 기본 근접 공격을 사용할 수 있는지 확인한다.
     *
     * @param combatUser 대상 플레이어
     * @return 근접 공격을 사용할 수 있으면 {@code true} 반환
     */
    public boolean canUseMeleeAttack(@NonNull CombatUser combatUser) {
        return true;
    }

    /**
     * 전투원이 달리기를 할 수 있는지 확인한다.
     *
     * @param combatUser 대상 플레이어
     * @return 달리기 가능 여부
     */
    public boolean canSprint(@NonNull CombatUser combatUser) {
        return true;
    }

    /**
     * 전투원이 비행할 수 있는지 확인한다.
     *
     * @param combatUser 대상 플레이어
     * @return 비행 가능 여부
     */
    public boolean canFly(@NonNull CombatUser combatUser) {
        return false;
    }

    /**
     * 전투원이 점프를 할 수 있는지 확인한다.
     *
     * @param combatUser 대상 플레이어
     * @return 점프 가능 여부
     */
    public boolean canJump(@NonNull CombatUser combatUser) {
        return true;
    }

    /**
     * 전투원의 무기 정보를 반환한다.
     *
     * @return 무기 정보
     */
    @NonNull
    public abstract WeaponInfo<?> getWeaponInfo();

    /**
     * 전투원의 특성 목록을 반환한다.
     *
     * @return 특성 목록. 길이가 0~4 사이인 배열
     */
    @NonNull
    public final TraitInfo @NonNull [] getTraitInfos() {
        return ArrayUtils.addAll(getDefaultTraitInfos(), getCombatantTraitInfos());
    }

    /**
     * 전투원의 역할군 기본 특성 목록을 반환한다.
     *
     * @return 특성 목록
     */
    @NonNull
    abstract TraitInfo @NonNull [] getDefaultTraitInfos();

    /**
     * 전투원의 개별 특성 목록을 반환한다.
     *
     * @return 특성 목록
     */
    @NonNull
    protected abstract TraitInfo @NonNull [] getCombatantTraitInfos();

    /**
     * 전투원의 모든 스킬 목록을 반환한다.
     *
     * @return 모든 스킬 목록. 길이가 0~8인 배열
     */
    @NonNull
    public final SkillInfo<?> @NonNull [] getSkillInfos() {
        return ArrayUtils.addAll(ArrayUtils.addAll(new SkillInfo[0], getPassiveSkillInfos()), getActiveSkillInfos());
    }

    /**
     * 전투원의 패시브 스킬 정보 목록을 반환한다.
     *
     * @return 패시브 스킬 정보 목록. 길이가 0~4 사이인 배열
     */
    @NonNull
    public abstract PassiveSkillInfo<?> @NonNull [] getPassiveSkillInfos();

    /**
     * 전투원의 액티브 스킬 정보 목록을 반환한다.
     *
     * @return 액티브 스킬 정보 목록. 길이가 0~4 사이인 배열
     */
    @NonNull
    public abstract ActiveSkillInfo<?> @NonNull [] getActiveSkillInfos();

    /**
     * 전투원의 궁극기 정보를 반환한다.
     *
     * @return 궁극기 정보
     */
    @NonNull
    public abstract UltimateSkillInfo<?> getUltimateSkillInfo();
}
