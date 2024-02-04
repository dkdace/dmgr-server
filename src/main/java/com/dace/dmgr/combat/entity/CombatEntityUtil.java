package com.dace.dmgr.combat.entity;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

/**
 * 전투 시스템의 엔티티 관련 기능을 제공하는 클래스.
 */
@UtilityClass
public final class CombatEntityUtil {
    /**
     * 게임에 소속되지 않은 모든 엔티티를 반환한다.
     *
     * @return 게임에 소속되지 않은 모든 엔티티
     */
    @NonNull
    public static CombatEntity[] getAllExcluded() {
        return CombatEntityRegistry.getInstance().getAllExcluded();
    }

    /**
     * 엔티티를 지정한 위치에 소환한다.
     *
     * @param entityClass 엔티티 클래스
     * @param location    소환할 위치
     * @param <T>         {@link LivingEntity}를 상속받는 엔티티 타입
     * @return 엔티티
     */
    @NonNull
    public static <T extends LivingEntity> T spawn(@NonNull Class<T> entityClass, @NonNull Location location) {
        T entity = location.getWorld().spawn(location, entityClass);
        if (entity.getVehicle() != null) {
            entity.getVehicle().remove();
            entity.leaveVehicle();
        }
        entity.getEquipment().clear();

        return entity;
    }
}
