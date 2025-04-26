package com.dace.dmgr.combat.entity.temporary.spawnhandler;

import com.dace.dmgr.combat.entity.temporary.TemporaryEntity;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

/**
 * 엔티티의 생성 로직을 처리하는 클래스.
 *
 * @param <T> {@link Entity}를 상속받는 엔티티 타입
 */
public interface EntitySpawnHandler<T extends Entity> {
    /**
     * 기본 엔티티 생성 처리기를 반환한다.
     *
     * <p>일부 엔티티의 경우 개별 구현체({@link PlayerNPCSpawnHandler} 등) 를 사용해야 한다.</p>
     *
     * @param entityClass 엔티티 클래스
     * @param <T>         {@link Entity}를 상속받는 엔티티 타입
     * @return 기본 엔티티 생성 처리기
     */
    @NonNull
    static <T extends Entity> EntitySpawnHandler<T> getDefaultSpawnHandler(@NonNull Class<T> entityClass) {
        return spawnLocation -> {
            try {
                return spawnLocation.getWorld().spawn(spawnLocation, entityClass, entity -> {
                    if (entity.getVehicle() != null) {
                        entity.getVehicle().remove();
                        entity.leaveVehicle();
                    }

                    if (entity instanceof LivingEntity)
                        ((LivingEntity) entity).getEquipment().clear();

                });
            } catch (IllegalArgumentException ex) {
                throw new IllegalStateException("해당 엔티티를 생성할 수 없음");
            }
        };
    }

    /**
     * 지정한 위치에 엔티티를 생성하여 반환한다.
     *
     * @param spawnLocation 생성 위치
     * @return 생성된 엔티티
     * @throws IllegalStateException {@code spawnLocation}에 엔티티를 소환할 수 없으면 발생
     */
    @NonNull
    T createEntity(@NonNull Location spawnLocation);

    /**
     * 엔티티가 성공적으로 생성되었을 때 실행할 작업.
     *
     * @param combatEntity 전투 시스템 엔티티
     */
    default void onSpawn(@NonNull TemporaryEntity<T> combatEntity) {
        // 미사용
    }
}
