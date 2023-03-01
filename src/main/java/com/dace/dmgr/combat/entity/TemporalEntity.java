package com.dace.dmgr.combat.entity;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import static com.dace.dmgr.system.HashMapList.temporalEntityMap;

/**
 * 일시적인 엔티티 클래스.
 *
 * <p>설랑, 포탈 등 전투에서 일시적으로 사용하는 엔티티를 말한다.</p>
 *
 * @param <T> {@link LivingEntity}를 상속받는 엔티티 타입
 */
public class TemporalEntity<T extends LivingEntity> extends CombatEntity<T> {
    /**
     * 엔티티 인스턴스를 생성하고 지정한 위치에 소환한다.
     *
     * @param entityType 엔티티 타입
     * @param name       이름
     * @param location   대상 위치
     * @param hitbox     히트박스
     */
    protected TemporalEntity(EntityType entityType, String name, Location location, Hitbox hitbox) {
        this(entityType, name, location, hitbox, null);
    }

    /**
     * 엔티티 인스턴스를 생성하고 지정한 위치에 소환한다.
     *
     * @param entityType 엔티티 타입
     * @param name       이름
     * @param location   대상 위치
     * @param hitbox     히트박스
     * @param critHitbox 치명타 히트박스
     */
    protected TemporalEntity(EntityType entityType, String name, Location location, Hitbox hitbox, Hitbox critHitbox) {
        super((T) location.getWorld().spawnEntity(location, entityType), name, hitbox, critHitbox);
        temporalEntityMap.put(getEntity(), this);
    }

    /**
     * 엔티티를 제거한다.
     */
    public void remove() {
        entity.setHealth(0);
        entity.remove();
    }
}
