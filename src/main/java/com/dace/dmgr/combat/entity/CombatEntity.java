package com.dace.dmgr.combat.entity;

import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.entity.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.statuseffect.StatusEffectType;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

/**
 * 전투 시스템의 엔티티 정보를 관리하는 인터페이스.
 *
 * @see CombatEntityBase
 */
public interface CombatEntity {
    /**
     * @param <T> {@link LivingEntity}를 상속받는 엔티티 타입
     * @return 엔티티 객체
     */
    <T extends LivingEntity> T getEntity();

    /**
     * @return 능력치 목록 관리 객체
     */
    AbilityStatusManager getAbilityStatusManager();

    /**
     * @return 속성 목록 관리 객체
     */
    PropertyManager getPropertyManager();

    /**
     * @return 히트박스 객체 목록
     */
    Hitbox[] getHitboxes();

    /**
     * @return 이름
     */
    String getName();

    String getTeam();

    void setTeam(String team);

    /**
     * @return 히트박스의 가능한 최대 크기
     */
    double getMaxHitboxSize();

    /**
     * {@link CombatEntityBase#init()} 호출 시 실행할 작업.
     */
    void onInit();

    /**
     * {@link CombatEntityBase#init()}에서 매 틱마다 실행될 작업.
     *
     * @param i 인덱스
     */
    void onTick(int i);

    /**
     * 지정한 엔티티가 적인 지 확인한다.
     *
     * @param combatEntity 대상 엔티티
     * @return 적이면 {@code true} 반환
     */
    boolean isEnemy(CombatEntity combatEntity);

    /**
     * 엔티티가 해당 위치를 통과할 수 있는 지 확인한다.
     *
     * @param location 대상 위치
     * @return 통과 가능하면 {@code true} 반환
     */
    boolean canPass(Location location);

    /**
     * 엔티티가 대상 엔티티의 위치를 통과할 수 있는 지 확인한다.
     *
     * @param combatEntity 대상 엔티티
     * @return 통과 가능하면 {@code true} 반환
     */
    boolean canPass(CombatEntity combatEntity);

    /**
     * 엔티티에게 상태 효과를 적용한다.
     *
     * <p>이미 해당 상태 효과를 가지고 있으면 새로 지정한 지속시간이
     * 남은 시간보다 길 경우에만 적용한다.</p>
     *
     * @param statusEffect 적용할 상태 효과
     * @param duration     지속시간 (tick)
     */
    void applyStatusEffect(StatusEffect statusEffect, long duration);

    /**
     * 엔티티의 지정한 상태 효과의 남은 시간을 반환한다.
     *
     * @param statusEffectType 확인할 상태 효과 종류
     * @return 남은 시간 (tick)
     */
    long getStatusEffectDuration(StatusEffectType statusEffectType);

    /**
     * 엔티티가 지정한 상태 효과를 가지고 있는 지 확인한다.
     *
     * @param statusEffectType 확인할 상태 효과 종류
     * @return 상태 효과를 가지고 있으면 {@code true} 반환
     */
    boolean hasStatusEffect(StatusEffectType statusEffectType);

    /**
     * 엔티티의 상태 효과를 제거한다.
     *
     * @param statusEffectType 제거할 상태 효과 종류
     */
    void removeStatusEffect(StatusEffectType statusEffectType);

    /**
     * 엔티티가 공격당했을 때 공격자에게 궁극기 게이지를 제공하는 지 확인한다.
     *
     * <p>기본값은 {@code false}이며, 오버라이딩하여 재설정할 수 있다.</p>
     *
     * @return 궁극기 제공 여부
     */
    default boolean isUltProvider() {
        return false;
    }

    /**
     * 엔티티가 죽을 수 있는 지 확인한다.
     *
     * <p>기본값은 {@code true}이며, 오버라이딩하여 재설정할 수 있다.</p>
     *
     * @return 죽을 수 있으면 {@code true} 반환
     */
    default boolean canDie() {
        return true;
    }

    /**
     * 엔티티가 기본 공격을 했을 때 실행될 작업.
     *
     * @param victim 피격자
     */
    default void onDefaultAttack(Damageable victim) {
    }

    /**
     * 엔티티가 다른 엔티티를 공격했을 때 실행될 작업.
     *
     * @param victim     피격자
     * @param damage     피해량
     * @param damageType 피해 타입
     * @param isCrit     치명타 여부
     * @param isUlt      궁극기 충전 여부
     * @see Damageable#onDamage(CombatEntity, int, DamageType, boolean, boolean)
     */
    default void onAttack(Damageable victim, int damage, DamageType damageType, boolean isCrit, boolean isUlt) {
    }

    /**
     * 엔티티가 다른 엔티티를 치유했을 때 실행될 작업.
     *
     * @param target 수급자
     * @param amount 치유량
     * @param isUlt  궁극기 충전 여부
     * @see Healable#onTakeHeal(CombatEntity, int, boolean)
     */
    default void onGiveHeal(Healable target, int amount, boolean isUlt) {
    }

    /**
     * 엔티티가 다른 엔티티를 죽였을 때 실행될 작업.
     *
     * @param victim 피격자
     * @see CombatEntity#onDeath(CombatEntity)
     */
    default void onKill(CombatEntity victim) {
    }

    /**
     * 엔티티가 죽었을 때 실행될 작업.
     *
     * @param attacker 공격자
     * @see CombatEntity#onKill(CombatEntity)
     */
    default void onDeath(CombatEntity attacker) {
    }
}
