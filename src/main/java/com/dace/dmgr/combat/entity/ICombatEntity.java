package com.dace.dmgr.combat.entity;

import org.bukkit.entity.Entity;

/**
 * 전투 시스템의 엔티티 정보를 관리하는 인터페이스.
 */
public interface ICombatEntity {
    Entity getEntity();

    void updateHitboxTick();

    Hitbox getHitbox();

    Hitbox getCritHitbox();

    String getTeam();

    void setTeam(String team);

    String getName();

    void setName(String name);

    int getHealth();

    void setHealth(int health);

    int getMaxHealth();

    void setMaxHealth(int health);

    int getSpeedIncrement();

    /**
     * 엔티티의 이동속도 증가량을 설정한다.
     *
     * @param speedIncrement 이동속도 증가량. 최소 값은 {@code -100}, 최대 값은 {@code 100}
     */
    void addSpeedIncrement(int speedIncrement);

    /**
     * 엔티티가 공격당했을 때 공격자에게 궁극기 게이지를 제공하는 지 확인한다.
     *
     * @return 궁극기 제공 여부
     */
    default boolean isUltProvider() {
        return false;
    }

    /**
     * 엔티티가 피해를 받을 수 있는 지 확인한다.
     *
     * @return 피격 가능 여부
     */
    default boolean isDamageable() {
        return true;
    }
}
