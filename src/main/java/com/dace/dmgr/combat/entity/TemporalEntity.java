package com.dace.dmgr.combat.entity;

import com.dace.dmgr.system.HashMapList;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import static com.dace.dmgr.system.HashMapList.temporalEntityMap;

/**
 * 일시적인 엔티티 클래스.
 *
 * <p>설랑, 포탈 등 전투에서 일시적으로 사용하는 엔티티를 말한다.</p>
 *
 * @param <T> {@link LivingEntity}를 상속받는 엔티티 타입
 */
public abstract class TemporalEntity<T extends LivingEntity> extends CombatEntity<T> {
    /** 최대 체력 */
    protected final int maxHealth;

    /**
     * 일시적 엔티티 인스턴스를 생성한다.
     *
     * @param name       이름
     * @param hitbox     히트박스
     * @param critHitbox 치명타 히트박스
     * @param isFixed    위치 고정 여부
     * @param maxHealth  최대 체력
     * @see HashMapList#temporalEntityMap
     */
    protected TemporalEntity(String name, Hitbox hitbox, Hitbox critHitbox, boolean isFixed, int maxHealth) {
        super(name, hitbox, critHitbox, isFixed);
        this.maxHealth = maxHealth;
    }

    /**
     * 엔티티를 지정한 위치에 소환하고 {@link HashMapList#temporalEntityMap}에 추가한다.
     *
     * @param entityType 엔티티 타입
     * @param location   대상 위치
     * @param health     체력
     * @see HashMapList#temporalEntityMap
     */
    public void spawn(Class<T> entityType, Location location, int health) {
        entity = location.getWorld().spawn(location, entityType);
        temporalEntityMap.put(getEntity(), this);
        init();
        setMaxHealth(maxHealth);
        setHealth(health);
        onSummon(location);
    }

    /**
     * 엔티티를 제거한다.
     */
    public void remove() {
        entity.setHealth(0);
        entity.remove();
    }

    /**
     * 엔티티를 소환했을 때 실행될 작업
     *
     * @param location 소환된 위치
     */
    protected abstract void onSummon(Location location);
}
