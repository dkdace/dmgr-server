package com.dace.dmgr.combat.entity;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

/**
 * 엔티티의 소환 및 제거 기능을 제공하는 클래스.
 */
public final class CombatEntityUtil {
    /**
     * 엔티티를 지정한 위치에 소환한다.
     *
     * @param entityClass 엔티티 클래스
     * @param location    소환할 위치
     * @param <T>         {@link LivingEntity}를 상속받는 엔티티 타입
     * @return 엔티티
     */
    public static <T extends LivingEntity> T spawn(Class<T> entityClass, Location location) {
        T entity = location.getWorld().spawn(location, entityClass);
        if (entity.getVehicle() != null) {
            entity.getVehicle().remove();
            entity.leaveVehicle();
        }
        entity.getEquipment().clear();

        return entity;
    }
}
