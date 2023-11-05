package com.dace.dmgr.combat.entity;

import com.dace.dmgr.combat.entity.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.statuseffect.StatusEffectType;
import com.dace.dmgr.game.Team;
import com.dace.dmgr.system.task.HasTask;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

/**
 * 전투 시스템의 엔티티 정보를 관리하는 인터페이스.
 *
 * @see CombatEntityBase
 */
public interface CombatEntity extends HasTask {
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

    Team getTeam();

    void setTeam(Team team);

    /**
     * @return 히트박스의 가능한 최대 크기
     */
    double getMaxHitboxSize();

    /**
     * 엔티티를 초기화하고 틱 스케쥴러를 실행한다.
     */
    void init();

    /**
     * 엔티티를 제거한다.
     */
    void remove();

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
     * 다른 엔티티가 이 엔티티를 대상으로 지정할 수 있는 지 확인한다.
     *
     * @return 지정할 수 있으면 {@code true} 반환
     */
    boolean canBeTargeted();
}
