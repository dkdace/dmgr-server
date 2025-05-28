package com.dace.dmgr.combat.entity;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link CombatEntity}를 저장하는 저장소 클래스.
 */
@UtilityClass
public final class CombatEntityRegistry {
    /** 전투 시스템 엔티티 목록 (엔티티 : 전투 시스템 엔티티) */
    private static final HashMap<Entity, CombatEntity> COMBAT_ENTITY_MAP = new HashMap<>();
    /** 월드별 전투 시스템 엔티티 목록 (월드 : (엔티티 : 전투 시스템 엔티티)) */
    private static final HashMap<World, HashMap<Entity, CombatEntity>> WORLD_COMBAT_ENTITY_MAP = new HashMap<>();

    /**
     * 전투 시스템 엔티티 추가 시 실행할 작업.
     *
     * @param combatEntity 전투 시스템 엔티티
     */
    void onAdd(@NonNull CombatEntity combatEntity) {
        Entity entity = combatEntity.getEntity();

        COMBAT_ENTITY_MAP.put(entity, combatEntity);
        WORLD_COMBAT_ENTITY_MAP.computeIfAbsent(combatEntity.getWorld(), k -> new HashMap<>()).put(entity, combatEntity);
    }

    /**
     * 전투 시스템 엔티티 제거 시 실행할 작업.
     *
     * @param combatEntity 전투 시스템 엔티티
     */
    void onRemove(@NonNull CombatEntity combatEntity) {
        Entity entity = combatEntity.getEntity();

        COMBAT_ENTITY_MAP.remove(entity);
        WORLD_COMBAT_ENTITY_MAP.get(combatEntity.getWorld()).remove(entity);
    }

    /**
     * 지정한 월드에 있는 모든 전투 시스템 엔티티를 반환한다.
     *
     * @param world 대상 월드
     * @return 모든 전투 시스템 엔티티
     */
    @NonNull
    @UnmodifiableView
    public static Collection<@NonNull CombatEntity> getAll(@NonNull World world) {
        HashMap<Entity, CombatEntity> combatEntityMap = WORLD_COMBAT_ENTITY_MAP.get(world);
        return Collections.unmodifiableCollection(combatEntityMap == null ? Collections.emptySet() : combatEntityMap.values());
    }

    /**
     * 지정한 엔티티의 전투 시스템 엔티티 인스턴스를 반환한다.
     *
     * @param entity 대상 엔티티
     * @return 전투 시스템의 엔티티 인스턴스. 존재하지 않으면 {@code null} 반환
     */
    @Nullable
    public static CombatEntity get(@NonNull Entity entity) {
        return COMBAT_ENTITY_MAP.get(entity);
    }

    /**
     * 지정한 위치를 기준으로 범위 안의 특정 조건을 만족하는 가장 가까운 엔티티를 반환한다.
     *
     * @param location        위치
     * @param range           범위 (반지름). (단위: 블록). 0 이상의 값
     * @param entityCondition 엔티티 탐색 조건
     * @param <T>             {@link CombatEntity}를 상속받는 전투 시스템 엔티티
     * @return 범위 내 가장 가까운 엔티티
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @see CombatEntityRegistry#getNearCombatEntities(Location, double, EntityCondition)
     */
    @Nullable
    public static <T extends CombatEntity> T getNearCombatEntity(@NonNull Location location, double range, @NonNull EntityCondition<T> entityCondition) {
        Validate.isTrue(range >= 0, "range >= 0 (%f)", range);

        return getAll(location.getWorld()).stream()
                .map(entityCondition::cast)
                .filter(combatEntity -> combatEntity != null && entityCondition.test(combatEntity) && combatEntity.canBeTargeted()
                        && combatEntity.isInHitbox(location, range))
                .findFirst()
                .orElse(null);
    }

    /**
     * 지정한 위치를 기준으로 범위 안의 특정 조건을 만족하는 모든 엔티티를 반환한다.
     *
     * @param location        위치
     * @param range           범위 (반지름). (단위: 블록). 0 이상의 값
     * @param entityCondition 엔티티 탐색 조건
     * @param <T>             {@link CombatEntity}를 상속받는 전투 시스템 엔티티
     * @return 범위 내 모든 엔티티
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @see CombatEntityRegistry#getNearCombatEntity(Location, double, EntityCondition)
     */
    @NonNull
    @UnmodifiableView
    public static <T extends CombatEntity> Set<@NonNull T> getNearCombatEntities(@NonNull Location location, double range,
                                                                                 @NonNull EntityCondition<T> entityCondition) {
        Validate.isTrue(range >= 0, "range >= 0 (%f)", range);

        return Collections.unmodifiableSet(getAll(location.getWorld()).stream()
                .map(entityCondition::cast)
                .filter(combatEntity -> combatEntity != null && entityCondition.test(combatEntity) && combatEntity.canBeTargeted()
                        && combatEntity.isInHitbox(location, range))
                .collect(Collectors.toSet()));
    }

    /**
     * 지정한 월드에서 특정 조건을 만족하는 모든 엔티티를 반환한다.
     *
     * @param world           대상 월드
     * @param entityCondition 엔티티 탐색 조건
     * @param <T>             {@link CombatEntity}를 상속받는 전투 시스템 엔티티
     * @return 범위 내 모든 엔티티
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    @NonNull
    @UnmodifiableView
    public static <T extends CombatEntity> Set<@NonNull T> getCombatEntities(@NonNull World world, @NonNull EntityCondition<T> entityCondition) {
        return Collections.unmodifiableSet(getAll(world).stream()
                .map(entityCondition::cast)
                .filter(combatEntity -> combatEntity != null && entityCondition.test(combatEntity))
                .collect(Collectors.toSet()));
    }
}
